package frontend.data.room

import androidx.room.*

@Dao
interface AttachmentDao {
    @Query("SELECT * FROM attachments WHERE owner_id = :ownerId AND owner_type = :ownerType ORDER BY created_at DESC")
    fun getForOwner(ownerId: String, ownerType: String): List<AttachmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AttachmentEntity): Long

    @Query("DELETE FROM attachments WHERE id = :id")
    suspend fun deleteById(id: String): Int
}

