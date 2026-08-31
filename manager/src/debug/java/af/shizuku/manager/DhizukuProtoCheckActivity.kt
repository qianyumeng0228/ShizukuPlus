package af.shizuku.manager

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.os.Parcel
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/**
 * Debug-only diagnostic: simulates the CURRENT official Dhizuku client SDK connection flow
 * against this app's own DhizukuProvider (authority = <pkg>.dhizuku_server.provider, resolved
 * from the device-owner package name) and exercises the V2 binder protocol.
 *
 * Results are printed to logcat under "DhizukuProtoCheck" and rendered on screen.
 */
class DhizukuProtoCheckActivity : Activity() {

    private val sb = StringBuilder()
    private lateinit var tv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)
        val ll = LinearLayout(this)
        ll.orientation = LinearLayout.VERTICAL
        ll.setPadding(32, 32, 32, 32)
        tv = TextView(this)
        tv.setTextColor(Color.BLACK)
        tv.textSize = 14f
        tv.typeface = Typeface.MONOSPACE
        ll.addView(tv)
        scroll.addView(ll)
        setContentView(scroll)

        try {
            run()
        } catch (t: Throwable) {
            log("EXCEPTION: $t")
        }
        tv.text = sb.toString()
    }

    private fun log(s: String) {
        sb.append(s).append('\n')
        Log.i("DhizukuProtoCheck", s)
    }

    private fun run() {
        // 1. resolve device owner (same as Dhizuku.getOwnerComponent)
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admins = dpm.activeAdmins
        var ownerComponent: ComponentName? = null
        if (admins != null) {
            for (a in admins) {
                if (dpm.isDeviceOwnerApp(a.packageName)) {
                    ownerComponent = a
                    break
                }
            }
        }
        log("device owner = $ownerComponent")
        if (ownerComponent == null) {
            log("FAIL: no device owner")
            return
        }

        // 2. authority (same as DhizukuVariables.getProviderAuthorityName)
        val pkg = ownerComponent.packageName
        val authority = if (pkg == "com.rosan.dhizuku") "com.rosan.dhizuku.server.provider" else "$pkg.dhizuku_server.provider"
        log("resolved authority = $authority")

        // 3. call provider "client" method (same as Dhizuku.init)
        val uri = Uri.Builder().scheme("content").authority(authority).build()
        val extras = Bundle()
        extras.putBinder("client", object : Binder() {})
        val result = try {
            contentResolver.call(uri, "client", null, extras)
        } catch (e: Exception) {
            log("call FAILED: ${e.javaClass.simpleName}: ${e.message}")
            return
        }
        if (result == null) {
            log("FAIL: call returned null")
            return
        }
        val binder = result.getBinder("dhizuku_binder")
        log("V2 binder = $binder")
        if (binder == null) {
            log("FAIL: no dhizuku_binder")
            return
        }

        val firstCall = 1

        // getVersionCode -> int
        log("== getVersionCode (code ${firstCall + 0}) ==")
        log("  " + transactInt(binder, "com.rosan.dhizuku.aidl.IDhizuku", firstCall + 0))

        // getVersionName -> String
        log("== getVersionName (code ${firstCall + 1}) ==")
        log("  " + transactString(binder, "com.rosan.dhizuku.aidl.IDhizuku", firstCall + 1))

        // isPermissionGranted -> boolean
        log("== isPermissionGranted (code ${firstCall + 2}) ==")
        log("  " + transactInt(binder, "com.rosan.dhizuku.aidl.IDhizuku", firstCall + 2))

        log("DONE")
    }

    private fun transactInt(binder: IBinder, iface: String, code: Int): String {
        return try {
            val data = Parcel.obtain()
            val reply = Parcel.obtain()
            data.writeInterfaceToken(iface)
            val ok = binder.transact(code, data, reply, 0)
            reply.readException()
            val v = reply.readInt()
            data.recycle()
            reply.recycle()
            "ok=$ok value=$v"
        } catch (t: Throwable) {
            "THROW ${t.javaClass.simpleName}: ${t.message}"
        }
    }

    private fun transactString(binder: IBinder, iface: String, code: Int): String {
        return try {
            val data = Parcel.obtain()
            val reply = Parcel.obtain()
            data.writeInterfaceToken(iface)
            val ok = binder.transact(code, data, reply, 0)
            reply.readException()
            val v = reply.readString()
            data.recycle()
            reply.recycle()
            "ok=$ok value=$v"
        } catch (t: Throwable) {
            "THROW ${t.javaClass.simpleName}: ${t.message}"
        }
    }
}
