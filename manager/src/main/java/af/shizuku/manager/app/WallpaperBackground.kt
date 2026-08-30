package af.shizuku.manager.app

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import af.shizuku.manager.R
import af.shizuku.manager.ShizukuSettings

/**
 * Shared page background used by both the home screen and the settings screen.
 *
 * Follows the user's wallpaper-theme setting:
 *  - white_miku: the bright Miku wallpaper (scrim adapts to system light/dark)
 *  - black_miku: the dark cyberpunk Miku wallpaper
 *  - original: no wallpaper — the stock animated gradient background
 *
 * A faint "breathing" gradient tint is layered on top of the wallpaper so the app keeps its
 * expressive feel even with a photographic background.
 */
@Composable
fun WallpaperBackground(content: @Composable () -> Unit) {
    val theme = ShizukuSettings.getWallpaperTheme()

    if (theme == ShizukuSettings.WALLPAPER_THEME_ORIGINAL) {
        OriginalGradientBackground(content)
        return
    }

    val wallpaperRes = if (theme == ShizukuSettings.WALLPAPER_THEME_BLACK_MIKU)
        R.drawable.wallpaper_bg_dark
    else
        R.drawable.wallpaper_bg_light

    val animationsEnabled = ShizukuSettings.isExpressiveAnimationsEnabled()
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (animationsEnabled) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val color1 = MaterialTheme.colorScheme.primary.copy(alpha = 0.03f + 0.03f * alpha)
    val color2 = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.03f + 0.03f * (1f - alpha))
    val color3 = MaterialTheme.colorScheme.secondary.copy(alpha = 0.025f)
    val color4 = MaterialTheme.colorScheme.primary.copy(alpha = 0.015f)

    val dark = isSystemInDarkTheme()
    val scrimColor = MaterialTheme.colorScheme.surface
    val scrim = Brush.verticalGradient(
        0f to scrimColor.copy(alpha = if (dark) 0.82f else 0.76f),
        0.4f to scrimColor.copy(alpha = if (dark) 0.66f else 0.52f),
        1f to scrimColor.copy(alpha = if (dark) 0.76f else 0.66f)
    )
    val breathe = Brush.sweepGradient(
        colors = listOf(color1, color2, color3, color4, color1),
        center = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(wallpaperRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(modifier = Modifier.fillMaxSize().background(scrim))
        Box(modifier = Modifier.fillMaxSize().background(breathe))
        content()
    }
}

/**
 * The stock (pre-wallpaper) animated gradient background, kept for the "original" wallpaper
 * option. Restores exactly the look the app had before the beautification.
 */
@Composable
fun OriginalGradientBackground(content: @Composable () -> Unit) {
    val animationsEnabled = ShizukuSettings.isExpressiveAnimationsEnabled()
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (animationsEnabled) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val color1 = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f + 0.06f * alpha)
    val color2 = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.05f + 0.06f * (1f - alpha))
    val color3 = MaterialTheme.colorScheme.secondary.copy(alpha = 0.04f)
    val color4 = MaterialTheme.colorScheme.primary.copy(alpha = 0.02f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.sweepGradient(
                    colors = listOf(color1, color2, color3, color4, color1),
                    center = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
    ) {
        content()
    }
}
