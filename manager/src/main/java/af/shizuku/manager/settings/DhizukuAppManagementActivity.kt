package af.shizuku.manager.settings

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import af.shizuku.core.ui.AppBarActivity
import af.shizuku.manager.R
import af.shizuku.manager.database.DhizukuAppManager
import af.shizuku.manager.widget.DhizukuAppsAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Dhizuku app management screen (ported from dhizuku's AppManagementPage).
 *
 * Lists apps that request the Dhizuku API permission, with an "allow access to Dhizuku"
 * switch and a block/unblock button per app. Authorizations are stored in
 * [DhizukuAppManager]'s Room database and enforced by DhizukuProvider.
 */
class DhizukuAppManagementActivity : AppBarActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: View
    private lateinit var adapter: DhizukuAppsAdapter

    private val packageReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            refresh()
        }
    }

    override fun getLayoutId(): Int = R.layout.dhizuku_app_management_activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.dhizuku_app_management_title)

        recyclerView = findViewById(R.id.recycler_view)
        emptyView = findViewById(R.id.empty_view)

        recyclerView.layoutManager = LinearLayoutManager(this)
        ViewCompat.setOnApplyWindowInsetsListener(recyclerView) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            view.setPadding(bars.left, view.paddingTop, bars.right, view.paddingBottom)
            insets
        }

        adapter = DhizukuAppsAdapter(this) { refresh() }
        recyclerView.adapter = adapter

        // Load list off the main thread
        refresh()

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }
        ContextCompat.registerReceiver(this, packageReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    private fun refresh() {
        lifecycleScope.launch {
            val items = withContext(Dispatchers.IO) {
                DhizukuAppManager.ensureInitialized(applicationContext)
                DhizukuAppsAdapter.collectItems(applicationContext)
            }
            if (isFinishing || isDestroyed) return@launch
            adapter.setItems(items)
            val isEmpty = items.isEmpty()
            recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
            emptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(packageReceiver)
        } catch (e: Exception) {
            Timber.w(e, "unregisterReceiver failed")
        }
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
