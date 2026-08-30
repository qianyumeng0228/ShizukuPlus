package af.shizuku.manager.home

import android.os.Bundle
import af.shizuku.core.ui.AppActivity
import af.shizuku.core.ui.compose.AppTheme

import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import af.shizuku.manager.ShizukuSettings
import af.shizuku.manager.app.ThemeHelper
import af.shizuku.manager.home.compose.SystemHubScreen

class SystemHubActivity : AppActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            AppTheme(
                darkTheme = isSystemInDarkTheme(),
                isBlackNightTheme = ThemeHelper.isBlackNightTheme(this),
                isOneUi = ShizukuSettings.isOneUiThemeEnabled()
            ) {
                SystemHubScreen(
                    onBackClick = { finish() }
                )
            }
        }
    }
}
