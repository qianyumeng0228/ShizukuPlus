package af.shizuku.manager.shell

import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.Parcel
import timber.log.Timber
import af.shizuku.manager.utils.Logger.LOGGER
import rikka.shizuku.Shizuku
import af.shizuku.manager.ShizukuSettings

object ShellBinderRequestHandler {

    /**
     * Handles a REQUEST_BINDER broadcast directly (auth-token fast path).
     * Extracts the callback binder from the intent extras and delivers the Shizuku binder to it.
     * [BinderRequestReceiver] calls [deliverBinder] directly for the no-token (rish/shell) case.
     */
    fun handleRequest(context: Context, intent: Intent, requireAuth: Boolean = false): Boolean {
        if (intent.action != "rikka.shizuku.intent.action.REQUEST_BINDER" &&
            intent.action != "${context.packageName}.intent.action.REQUEST_BINDER") {
            return false
        }

        if (requireAuth) {
            val rawToken = intent.getStringExtra("auth")
            val authToken = if (rawToken != null) af.shizuku.manager.utils.IntentCrypto.decrypt(rawToken) else null
            val expectedToken = ShizukuSettings.getAuthToken()
            // Constant-time compare: this gates handing the live Shizuku binder to the caller, so a
            // length/early-exit-dependent compare would leak a timing side-channel on the token.
            if (authToken == null ||
                !java.security.MessageDigest.isEqual(authToken.toByteArray(), expectedToken.toByteArray())
            ) {
                return false
            }
        }

        val callbackBinder = intent.getBundleExtra("data")?.getBinder("binder") ?: return false
        return deliverBinder(context, callbackBinder)
    }

    /**
     * Delivers the Shizuku server binder to [callbackBinder] (the rish process's receiver).
     * Delivery itself is unconditional and makes no authorization decision — every privileged
     * AIDL method is separately gated by [enforceCallingPermission], keyed off the real,
     * kernel-verified uid of whatever transaction the caller makes once attached.
     */
    fun deliverBinder(context: Context, callbackBinder: IBinder): Boolean {
        val shizukuBinder = try {
            Shizuku.getBinder()
        } catch (e: Exception) {
            LOGGER.w(e, "getBinder failed")
            return false
        }
        if (shizukuBinder == null) {
            LOGGER.w("shizuku binder is null")
            return false
        }

        // The rish app_process may be frozen by Android's Cached Apps Freezer if the caller was
        // backgrounded while waiting for the consent notification. A frozen-process ONEWAY transact
        // returns an error immediately (BR_FROZEN_REPLY) rather than queuing. Retry with short
        // delays to give the system time to unfreeze the process (typically <1 s after the freeze
        // is lifted by the user foregrounding the caller or by normal OS scheduling).
        val retryDelaysMs = longArrayOf(0L, 200L, 600L, 1500L)
        var lastException: Exception? = null
        // Obtain once; reset and re-write on each retry to avoid repeated pool hits.
        val data = Parcel.obtain()
        try {
            for (delayMs in retryDelaysMs) {
                if (delayMs > 0) Thread.sleep(delayMs)
                // Must match ShizukuShellLoader.receiverBinder.onTransact exactly: it reads
                // readStrongBinder() then readString() with no enforceInterface() call, so a
                // leading writeInterfaceToken() here gets misread as the binder slot and an
                // omitted sourceDir NPEs the client at onBinderReceived's sourceDir.substring().
                data.setDataPosition(0)
                data.writeStrongBinder(shizukuBinder)
                data.writeString(context.applicationInfo.sourceDir)
                // ONEWAY — binder driver never writes into reply; null avoids unnecessary pool hit.
                try {
                    callbackBinder.transact(IBinder.FIRST_CALL_TRANSACTION, data, null, IBinder.FLAG_ONEWAY)
                    return true
                } catch (e: Exception) {
                    LOGGER.w(e, "transact attempt failed (delay was ${delayMs}ms)")
                    lastException = e
                }
            }
        } finally {
            data.recycle()
        }
        LOGGER.w(lastException, "transact: all attempts failed")
        return false
    }
}
