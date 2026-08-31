package af.shizuku.manager.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity storing Dhizuku access authorization for an app (mirrors dhizuku's AppEntity).
 *
 * @property id Auto-generated unique identifier.
 * @property uid UID of the requesting app.
 * @property signature Signature of the app at the time of grant (used to detect re-signing).
 * @property allowApi Whether the app may use the Dhizuku (device-owner) API.
 * @property blocked Whether the app is blocked from using Dhizuku entirely.
 * @property createdAt Creation timestamp.
 * @property modifiedAt Last modification timestamp.
 */
@Entity(
    tableName = "dhizuku_apps",
    indices = [
        Index(value = ["uid"], unique = true),
        Index(value = ["allowApi"])
    ]
)
data class DhizukuAppRoom(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uid: Int,
    val signature: String,
    val allowApi: Boolean,
    val blocked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
)
