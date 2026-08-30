package af.shizuku.manager.settings

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import af.shizuku.manager.R
import af.shizuku.manager.settings.compose.SettingsScreen
import af.shizuku.core.ui.AppActivity

class SettingsActivity : AppActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private var currentTitle by mutableStateOf("")
    private var searchResults by mutableStateOf<List<SettingsSearchEngine.SettingItem>>(emptyList())
    var themeVersion by mutableStateOf(0)

    fun onThemeChanged() {
        themeVersion++
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)

        SettingsSearchEngine.init(this)

        currentTitle = getString(R.string.settings_title)

        setContent {
            val tv = themeVersion
            af.shizuku.core.ui.compose.AppTheme(
                isBlackNightTheme = af.shizuku.manager.app.ThemeHelper.isBlackNightTheme(this),
                isOneUi = af.shizuku.manager.ShizukuSettings.isOneUiThemeEnabled(),
                themeVersion = tv
            ) {
                SettingsScreen(
                    title = currentTitle,
                    onNavigateUp = {
                        if (!onSupportNavigateUp()) {
                            finish()
                        }
                    },
                    onNavigateToSetting = { item -> navigateToSetting(item) },
                    searchResults = searchResults,
                    onSearchQueryChanged = { query ->
                        if (query.isBlank()) {
                            searchResults = emptyList()
                        } else {
                            searchResults = SettingsSearchEngine.search(this, query)
                        }
                    },
                    onContainerCreated = {
                        val frag = supportFragmentManager.findFragmentById(R.id.fragment_container)
                        val container = findViewById<android.view.ViewGroup>(R.id.fragment_container)
                        // On recreation after a theme/wallpaper switch (setDefaultNightMode triggers
                        // activity recreate), the FragmentManager restores the fragment but its view
                        // is left orphaned (container has 0 children) because Compose creates the
                        // container after the fragment-restoration phase. Re-parent the orphaned view
                        // so the settings modules don't vanish (blank page with only the wallpaper).
                        if (container == null || container.childCount == 0) {
                            val orphaned = frag?.view
                            if (container != null && orphaned != null) {
                                (orphaned.parent as? android.view.ViewGroup)?.removeView(orphaned)
                                container.addView(
                                    orphaned,
                                    android.view.ViewGroup.LayoutParams(
                                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                )
                            } else if (frag != null) {
                                supportFragmentManager.beginTransaction()
                                    .detach(frag)
                                    .attach(frag)
                                    .commit()
                            } else {
                                supportFragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, SettingsFragment())
                                    .commit()
                            }
                        }
                    }
                )
            }
        }
    }

    private fun navigateToSetting(item: SettingsSearchEngine.SettingItem) {
        val fragment = supportFragmentManager.fragmentFactory.instantiate(classLoader, item.fragmentClass)
        fragment.arguments = Bundle().apply {
            putString("highlight_key", item.key)
        }

        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()

        currentTitle = item.title
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        val fragmentName = pref.fragment ?: return false
        val fragment = supportFragmentManager.fragmentFactory.instantiate(classLoader, fragmentName)
        fragment.arguments = pref.extras

        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()

        currentTitle = pref.title?.toString() ?: currentTitle
        return true
    }

    fun updateTitle(title: String) {
        currentTitle = title
    }

    override fun onSupportNavigateUp(): Boolean {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            return true
        }
        return super.onSupportNavigateUp()
    }
}
