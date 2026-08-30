package af.shizuku.manager.ktx

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.UserManager
import android.util.Pair
import android.util.TypedValue
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import af.shizuku.manager.ShizukuApplication

fun Activity.startWithSceneTransition(intent: Intent, sharedView: View, transitionName: String) {
    val options = ActivityOptions.makeSceneTransitionAnimation(this, Pair.create(sharedView, transitionName))
    startActivity(intent, options.toBundle())
}

@ColorInt
fun Context.themeColor(@AttrRes attr: Int): Int {
    val tv = TypedValue()
    theme.resolveAttribute(attr, tv, true)
    return tv.data
}

/**
 * Alpha applied to card surfaces while a Miku wallpaper theme is active, so the wallpaper shows
 * through the cards. The original theme keeps the stock opaque colorSurfaceContainerHigh.
 */
private const val CARD_BG_ALPHA = 0.72f

/**
 * The background color for card surfaces. Under the Miku wallpaper themes (white/black) the cards
 * are made translucent so the wallpaper shows through; the "original" theme keeps the stock opaque
 * colorSurfaceContainerHigh exactly as it was before the beautification.
 */
@ColorInt
fun Context.cardSurfaceColor(): Int {
    val base = themeColor(com.google.android.material.R.attr.colorSurfaceContainerHigh)
    val theme = af.shizuku.manager.ShizukuSettings.getWallpaperTheme()
    if (theme == af.shizuku.manager.ShizukuSettings.WALLPAPER_THEME_ORIGINAL) return base
    return androidx.core.graphics.ColorUtils.setAlphaComponent(base, (CARD_BG_ALPHA * 255f).toInt())
}

/**
 * Resolves a `?attr/shapeAppearanceCorner*` theme attribute to its absolute corner size in
 * pixels, so Canvas-drawn (non-MaterialCardView) shapes can follow the Shape Style setting too.
 * Assumes the referenced ShapeAppearance uses an absolute cornerSize (true for every
 * ThemeOverlay.Shape.* variant in this app) — a percentage-based corner would need real bounds.
 */
fun Context.themeCornerSizePx(@AttrRes attr: Int): Float {
    val tv = TypedValue()
    theme.resolveAttribute(attr, tv, true)
    val shapeAppearanceModel = com.google.android.material.shape.ShapeAppearanceModel
        .builder(this, 0, tv.resourceId)
        .build()
    return shapeAppearanceModel.topLeftCornerSize.getCornerSize(android.graphics.RectF())
}

val Context.application: ShizukuApplication
    get() {
        return applicationContext as ShizukuApplication
    }

fun Context.createDeviceProtectedStorageContextCompat(): Context {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        createDeviceProtectedStorageContext()
    } else {
        this
    }
}

fun Context.createDeviceProtectedStorageContextCompatWhenLocked(): Context {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && getSystemService(UserManager::class.java)?.isUserUnlocked != true) {
        createDeviceProtectedStorageContext()
    } else {
        this
    }
}
