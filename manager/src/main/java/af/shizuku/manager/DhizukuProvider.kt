package af.shizuku.manager

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.os.ServiceManager
import com.rosan.dhizuku.IDhizuku
import af.shizuku.manager.utils.ShizukuStateMachine
import rikka.shizuku.Shizuku

class DhizukuProvider : ContentProvider() {

    /**
     * Dhizuku access check.
     *
     * Preference order:
     *  1. This app itself is always allowed.
     *  2. If a DhizukuAppManager authorization record exists (the new app-management
     *     mechanism), its allowApi / blocked / signature state decides.
     *  3. Otherwise fall back to the legacy Shizuku authorization (AuthorizationManager)
     *     so previously granted apps keep working.
     */
    private fun isCallerAllowedToUseDhizuku(callingUid: Int): Boolean {
        if (callingUid == android.os.Process.myUid()) return true
        val ctx = context ?: return false

        // New app-management mechanism
        af.shizuku.manager.database.DhizukuAppManager.ensureInitialized(ctx)
        val entity = af.shizuku.manager.database.DhizukuAppManager.findByUid(callingUid)
        if (entity != null) {
            if (!entity.allowApi || entity.blocked) return false
            // Signature must still match what was recorded at grant time.
            // NOTE: the stored value is the SHA-256 hex fingerprint (see DhizukuAppsAdapter),
            // so we must compute the same hash here — comparing against Signature.toCharsString()
            // would never match and would silently deny every authorized app.
            val pkgName = ctx.packageManager.getPackagesForUid(callingUid)?.firstOrNull() ?: return false
            val currentSig = try {
                val info = ctx.packageManager.getPackageInfo(
                    pkgName, android.content.pm.PackageManager.GET_SIGNATURES
                )
                val signatures = info.signatures ?: return false
                if (signatures.isEmpty()) return false
                val md = java.security.MessageDigest.getInstance("SHA-256")
                md.update(signatures[0].toByteArray())
                md.digest().joinToString("") { String.format("%02x", it) }
            } catch (e: Exception) {
                null
            }
            return currentSig != null && currentSig == entity.signature
        }

        // Legacy fallback
        val pkgName = ctx.packageManager.getPackagesForUid(callingUid)?.firstOrNull() ?: return false
        return af.shizuku.manager.authorization.AuthorizationManager.granted(pkgName, callingUid)
    }

    private inner class DhizukuV1Binder : IDhizuku.Stub() {
        override fun getVersion(): Int = 1

        override fun getBinder(): IBinder? {
            if (!ShizukuSettings.isDhizukuModeEnabled()) return null
            if (!ShizukuStateMachine.isRunning()) return null

            val callingUid = Binder.getCallingUid()
            if (!isCallerAllowedToUseDhizuku(callingUid)) {
                return null
            }

            return try {
                ServiceManager.getService(Context.DEVICE_POLICY_SERVICE)
            } catch (e: Exception) {
                null
            }
        }

        override fun isPermissionGranted(): Boolean {
            if (!ShizukuSettings.isDhizukuModeEnabled()) return false
            val callingUid = Binder.getCallingUid()
            return isCallerAllowedToUseDhizuku(callingUid)
        }

        override fun transact(code: Int, data: Bundle?): Bundle {
            return Bundle()
        }
    }

    private inner class DhizukuV2Binder : Binder() {
        override fun getInterfaceDescriptor(): String = "com.rosan.dhizuku.aidl.IDhizuku"

        // Mirrors DhizukuV1Binder's authorization gate above - the caller must either be
        // this app itself or a package the user has explicitly granted via Dhizuku app management
        // (falling back to the legacy Shizuku authorization).
        private fun isCallerAuthorized(): Boolean {
            val callingUid = Binder.getCallingUid()
            return isCallerAllowedToUseDhizuku(callingUid)
        }

        // Matches the CURRENT official Dhizuku client SDK (com.rosan.dhizuku.aidl.IDhizuku):
        //   transaction code = FIRST_CALL_TRANSACTION + interface index
        //   0 getVersionCode():int | 1 getVersionName():String | 2 isPermissionGranted():boolean
        //   11 remoteProcess | 12 bindUserService | 13 unbindUserService
        //   14 unbindUserServiceByConnection | 15 getDelegatedScopes | 16 setDelegatedScopes
        // The remote-binder relay (DhizukuVariables.TRANSACT_CODE_REMOTE_BINDER = FIRST_CALL_TRANSACTION+10)
        // is invoked through Binder.transact directly with descriptor "com.rosan.dhizuku.server".
        override fun onTransact(code: Int, data: android.os.Parcel, reply: android.os.Parcel?, flags: Int): Boolean {
            if (!ShizukuSettings.isDhizukuModeEnabled()) return false

            if (code == FIRST_CALL_TRANSACTION + 10) { // TRANSACT_CODE_REMOTE_BINDER
                data.enforceInterface("com.rosan.dhizuku.server")
                // Unlike the interface-token check above, this doesn't verify caller identity - without
                // isCallerAuthorized() any installed app could relay an arbitrary transact() call through
                // this process's identity onto a binder of its own choosing (confused-deputy).
                if (!isCallerAuthorized()) return false
                val targetBinder = data.readStrongBinder()
                val targetCode = data.readInt()
                val targetFlags = data.readInt()
                return targetBinder.transact(targetCode, data, reply, targetFlags)
            }

            data.enforceInterface("com.rosan.dhizuku.aidl.IDhizuku")
            when (code) {
                FIRST_CALL_TRANSACTION + 0 -> { // getVersionCode
                    reply?.writeNoException()
                    reply?.writeInt(7)
                    return true
                }
                FIRST_CALL_TRANSACTION + 1 -> { // getVersionName
                    reply?.writeNoException()
                    reply?.writeString("7")
                    return true
                }
                FIRST_CALL_TRANSACTION + 2 -> { // isPermissionGranted
                    reply?.writeNoException()
                    reply?.writeInt(if (isCallerAuthorized()) 1 else 0)
                    return true
                }
                FIRST_CALL_TRANSACTION + 11 -> { // remoteProcess -> not implemented
                    reply?.writeNoException()
                    reply?.writeStrongBinder(null)
                    return true
                }
                FIRST_CALL_TRANSACTION + 12, // bindUserService
                FIRST_CALL_TRANSACTION + 13, // unbindUserService
                FIRST_CALL_TRANSACTION + 14, // unbindUserServiceByConnection
                FIRST_CALL_TRANSACTION + 16 -> { // setDelegatedScopes (void methods)
                    reply?.writeNoException()
                    return true
                }
                FIRST_CALL_TRANSACTION + 15 -> { // getDelegatedScopes -> empty
                    reply?.writeNoException()
                    reply?.writeStringArray(arrayOf())
                    return true
                }
            }

            return super.onTransact(code, data, reply, flags)
        }
    }

    override fun onCreate(): Boolean = true

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        if ("getBinder" == method) {
            val bundle = Bundle()
            bundle.putBinder("binder", DhizukuV1Binder())
            return bundle
        }
        if ("client" == method) {
            val bundle = Bundle()
            bundle.putBinder("dhizuku_binder", DhizukuV2Binder())
            return bundle
        }
        return null
    }
}
