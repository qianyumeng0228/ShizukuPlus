package af.shizuku.manager.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Dhizuku app authorizations.
 */
@Dao
interface DhizukuAppDao {

    @Query("SELECT * FROM dhizuku_apps WHERE uid = :uid LIMIT 1")
    fun findByUid(uid: Int): DhizukuAppRoom?

    @Query("SELECT * FROM dhizuku_apps WHERE uid = :uid LIMIT 1")
    fun findByUidFlow(uid: Int): Flow<DhizukuAppRoom?>

    @Query("SELECT * FROM dhizuku_apps")
    fun getAll(): Flow<List<DhizukuAppRoom>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: DhizukuAppRoom): Long

    @Update
    fun update(entity: DhizukuAppRoom)

    @Query("DELETE FROM dhizuku_apps WHERE uid = :uid")
    fun deleteByUid(uid: Int)

    @Query("DELETE FROM dhizuku_apps")
    fun clear()
}
