package af.shizuku.manager.adb

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import af.shizuku.manager.R
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean

class FakeAdbPairingActivity : Activity() {

    companion object {
        private var currentLatch: CountDownLatch? = null
        private var currentResult = AtomicBoolean(false)

        fun requestPairingSync(context: Context, pubKeyStr: String): Boolean {
            val latch = CountDownLatch(1)
            currentLatch = latch
            currentResult.set(false)

            val intent = Intent(context, FakeAdbPairingActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("pubKey", pubKeyStr)
            }
            context.startActivity(intent)

            try {
                latch.await()
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
            return currentResult.get()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pubKeyStr = intent.getStringExtra("pubKey") ?: ""
        val hash = pubKeyStr.hashCode().toString(16).uppercase()

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.fake_adb_pairing_title)
            .setMessage(getString(R.string.fake_adb_pairing_message, hash))
            .setPositiveButton(R.string.fake_adb_pairing_allow) { _, _ ->
                currentResult.set(true)
                currentLatch?.countDown()
                finish()
            }
            .setNegativeButton(R.string.fake_adb_pairing_deny) { _, _ ->
                currentResult.set(false)
                currentLatch?.countDown()
                finish()
            }
            .setOnCancelListener {
                currentResult.set(false)
                currentLatch?.countDown()
                finish()
            }
            .setCancelable(false)
            .show()
    }
}
