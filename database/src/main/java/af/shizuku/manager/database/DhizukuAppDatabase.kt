package af.shizuku.manager.database

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Room database for Dhizuku app access authorizations.
 *
 * Stores which apps may use the Dhizuku (device-owner) API, mirroring the
 * standalone dhizuku app's authorization table.
 */
@Database(
    entities = [DhizukuAppRoom::class],
    version = 1,
    exportSchema = false
)
abstract class DhizukuAppDatabase : RoomDatabase() {

    abstract fun dhizukuAppDao(): DhizukuAppDao

    companion object {
        private const val DATABASE_NAME = "shizuku_dhizuku_apps.db"

        @Volatile
        private var instance: DhizukuAppDatabase? = null

        private val lock = ReentrantLock()

        fun getInstance(context: Context): DhizukuAppDatabase {
            return instance ?: lock.withLock {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): DhizukuAppDatabase =
            buildRoomDatabaseWithStorageFallback(
                context, DATABASE_NAME, DhizukuAppDatabase::class.java, "DhizukuAppDatabase"
            )

        fun resetInstance() {
            lock.withLock {
                instance?.close()
                instance = null
            }
        }
    }
}
