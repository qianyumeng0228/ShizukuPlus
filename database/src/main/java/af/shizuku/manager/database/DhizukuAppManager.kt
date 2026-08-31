package af.shizuku.manager.database

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Manager for Dhizuku app access authorizations.
 *
 * Provides synchronous lookups for the Dhizuku binder (which runs on binder threads)
 * and Flow-based data for the management UI. All Room calls are marshalled to the IO
 * dispatcher so callers (including the UI thread) never trip Room's
 * "cannot access database on the main thread" guard.
 */
object DhizukuAppManager {

    private const val TAG = "DhizukuAppManager"

    @Volatile
    private var appContext: Context? = null
    private var dao: DhizukuAppDao? = null
    private val isInitialized = AtomicBoolean(false)
    private val initLock = Any()

    /**
     * Initialize lazily. Safe to call multiple times; only the first call performs setup.
     */
    fun ensureInitialized(context: Context) {
        if (isInitialized.get()) return
        synchronized(initLock) {
            if (isInitialized.get()) return
            try {
                appContext = context.applicationContext
                dao = DhizukuAppDatabase.getInstance(context.applicationContext).dhizukuAppDao()
                isInitialized.set(true)
                Timber.tag(TAG).d("DhizukuAppManager initialized")
            } catch (e: Exception) {
                Timber.tag(TAG).w(e, "Failed to initialize DhizukuAppManager")
            }
        }
    }

    private fun dao(): DhizukuAppDao? {
        if (!isInitialized.get()) {
            val ctx = appContext
            if (ctx != null) ensureInitialized(ctx)
        }
        return dao
    }

    /**
     * Synchronous lookup by UID — used from the Dhizuku binder check and the UI.
     */
    fun findByUid(uid: Int): DhizukuAppRoom? {
        val d = dao() ?: return null
        return try {
            runBlocking(Dispatchers.IO) { d.findByUid(uid) }
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "findByUid failed")
            null
        }
    }

    /**
     * Flow of all authorization records — used by the management UI.
     */
    fun getAll(): Flow<List<DhizukuAppRoom>> {
        val d = dao() ?: return flowOf(emptyList())
        return d.getAll()
    }

    /**
     * Set whether an app may use the Dhizuku API. Keeps the existing blocked state.
     */
    fun setAllowed(uid: Int, signature: String, allowed: Boolean) {
        val d = dao() ?: return
        try {
            runBlocking(Dispatchers.IO) {
                val existing = d.findByUid(uid)
                if (existing == null) {
                    d.insert(
                        DhizukuAppRoom(
                            uid = uid,
                            signature = signature,
                            allowApi = allowed,
                            blocked = false,
                            createdAt = System.currentTimeMillis(),
                            modifiedAt = System.currentTimeMillis()
                        )
                    )
                } else {
                    d.update(
                        existing.copy(
                            signature = signature,
                            allowApi = allowed,
                            modifiedAt = System.currentTimeMillis()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "setAllowed failed")
        }
    }

    /**
     * Set whether an app is blocked from using the Dhizuku API.
     * Blocking also revokes allowApi (matching dhizuku behavior).
     */
    fun setBlocked(uid: Int, signature: String, blocked: Boolean) {
        val d = dao() ?: return
        try {
            runBlocking(Dispatchers.IO) {
                val existing = d.findByUid(uid)
                if (existing == null) {
                    d.insert(
                        DhizukuAppRoom(
                            uid = uid,
                            signature = signature,
                            allowApi = false,
                            blocked = blocked,
                            createdAt = System.currentTimeMillis(),
                            modifiedAt = System.currentTimeMillis()
                        )
                    )
                } else {
                    d.update(
                        existing.copy(
                            signature = signature,
                            blocked = blocked,
                            allowApi = if (blocked) false else existing.allowApi,
                            modifiedAt = System.currentTimeMillis()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "setBlocked failed")
        }
    }

    /**
     * Whether an app is allowed to use the Dhizuku API, matching dhizuku's check:
     * record must exist, allowApi true, not blocked.
     */
    fun isAllowed(uid: Int): Boolean {
        val entity = findByUid(uid) ?: return false
        return entity.allowApi && !entity.blocked
    }
}
