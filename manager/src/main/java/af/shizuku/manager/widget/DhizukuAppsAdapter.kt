package af.shizuku.manager.widget

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import af.shizuku.manager.R
import af.shizuku.manager.database.DhizukuAppManager
import af.shizuku.manager.database.DhizukuAppRoom
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.flow.first

/**
 * Adapter listing apps that request Dhizuku API access, mirroring dhizuku's AppManagementPage.
 *
 * Each row shows: app icon + label + package, an "allow access to Dhizuku" switch, and a
 * block/unblock button. Blocked rows are greyed out with the switch hidden.
 */
class DhizukuAppsAdapter(
    private val context: Context,
    private val onChanged: () -> Unit
) : RecyclerView.Adapter<DhizukuAppsAdapter.AppViewHolder>() {

    data class AppItem(
        val packageName: String,
        val uid: Int,
        val label: String,
        val icon: Drawable?,
        val record: DhizukuAppRoom?
    ) {
        val allowApi: Boolean get() = record?.allowApi ?: false
        val blocked: Boolean get() = record?.blocked ?: false
    }

    private val items: MutableList<AppItem> = mutableListOf()

    fun setItems(newItems: List<AppItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dhizuku_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView.findViewById(R.id.card)
        private val icon: ImageView = itemView.findViewById(R.id.app_icon)
        private val label: TextView = itemView.findViewById(R.id.app_label)
        private val packageName: TextView = itemView.findViewById(R.id.app_package)
        private val allowRow: LinearLayout = itemView.findViewById(R.id.allow_row)
        private val allowSwitch: SwitchMaterial = itemView.findViewById(R.id.allow_switch)
        private val blockedNotice: LinearLayout = itemView.findViewById(R.id.blocked_notice)
        private val blockButton: MaterialButton = itemView.findViewById(R.id.block_button)

        fun bind(item: AppItem) {
            icon.setImageDrawable(item.icon)
            label.text = item.label
            packageName.text = item.packageName

            // Blocked state: grey card, hide switch, show notice
            val blocked = item.blocked
            card.alpha = if (blocked) 0.5f else 1f
            allowRow.visibility = if (blocked) View.GONE else View.VISIBLE
            blockedNotice.visibility = if (blocked) View.VISIBLE else View.GONE
            blockButton.setText(
                if (blocked) R.string.dhizuku_unblock_app else R.string.dhizuku_block_app
            )

            // Switch state
            allowSwitch.isChecked = item.allowApi
            allowSwitch.setOnCheckedChangeListener(null)
            allowSwitch.setOnCheckedChangeListener { _, isChecked ->
                val sig = getAppSignature(context, item.uid) ?: ""
                DhizukuAppManager.setAllowed(item.uid, sig, isChecked)
                onChanged()
            }

            // Block / unblock button
            blockButton.setOnClickListener {
                val sig = getAppSignature(context, item.uid) ?: ""
                DhizukuAppManager.setBlocked(item.uid, sig, !blocked)
                onChanged()
            }
        }
    }

    companion object {
        /**
         * Compute the SHA-256 signature fingerprint of the app owning [uid], used to verify
         * the calling app hasn't been re-signed after authorization (same as dhizuku).
         */
        fun getAppSignature(context: Context, uid: Int): String? {
            return try {
                val pm = context.packageManager
                val pkgName = pm.getPackagesForUid(uid)?.firstOrNull() ?: return null
                val info = pm.getPackageInfo(pkgName, PackageManager.GET_SIGNATURES)
                val signatures = info.signatures ?: return null
                if (signatures.isEmpty()) return null
                val md = java.security.MessageDigest.getInstance("SHA-256")
                md.update(signatures[0].toByteArray())
                md.digest().joinToString("") { String.format("%02x", it) }
            } catch (e: Exception) {
                null
            }
        }

        /**
         * Collect apps to show: installed apps that declare the Dhizuku API permission,
         * plus any apps that already have an authorization record.
         */
        fun collectItems(context: Context): List<AppItem> {
            val pm = context.packageManager
            val items = mutableListOf<AppItem>()
            val seen = mutableSetOf<Int>()

            // 1. Installed apps declaring the Dhizuku API permission
            try {
                val allApps = pm.getInstalledApplications(
                    PackageManager.GET_PERMISSIONS or PackageManager.MATCH_UNINSTALLED_PACKAGES
                )
                for (app in allApps) {
                    val requested = try {
                        pm.getPackageInfo(app.packageName, PackageManager.GET_PERMISSIONS)?.requestedPermissions
                    } catch (e: Exception) {
                        null
                    }
                    val usesDhizuku = requested?.contains("com.rosan.dhizuku.permission.API") == true
                    if (usesDhizuku) {
                        addItem(pm, items, seen, app)
                    }
                }
            } catch (e: Exception) {
                // ignore
            }

            // 2. Apps with an existing authorization record (even if permission no longer declared)
            try {
                val records = kotlinx.coroutines.runBlocking {
                    DhizukuAppManager.getAll().first()
                }
                for (record in records) {
                    val pkgs = pm.getPackagesForUid(record.uid) ?: continue
                    if (pkgs.isEmpty()) continue
                    val app = try {
                        pm.getApplicationInfo(pkgs[0], 0)
                    } catch (e: Exception) {
                        continue
                    }
                    if (!seen.contains(record.uid)) {
                        items.add(
                            AppItem(
                                packageName = pkgs[0],
                                uid = record.uid,
                                label = pm.getApplicationLabel(app)?.toString() ?: pkgs[0],
                                icon = pm.getApplicationIcon(app),
                                record = record
                            )
                        )
                        seen.add(record.uid)
                    }
                }
            } catch (e: Exception) {
                // ignore
            }

            items.sortBy { it.label.lowercase() }
            return items
        }

        private fun addItem(
            pm: PackageManager,
            items: MutableList<AppItem>,
            seen: MutableSet<Int>,
            app: ApplicationInfo
        ) {
            val uid = app.uid
            if (!seen.add(uid)) return
            val record = DhizukuAppManager.findByUid(uid)
            items.add(
                AppItem(
                    packageName = app.packageName,
                    uid = uid,
                    label = pm.getApplicationLabel(app)?.toString() ?: app.packageName,
                    icon = try {
                        pm.getApplicationIcon(app)
                    } catch (e: Exception) {
                        null
                    },
                    record = record
                )
            )
        }
    }
}
