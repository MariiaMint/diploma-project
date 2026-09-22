package frontend.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders ORDER BY name COLLATE NOCASE")
    fun getAll(): List<FolderEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: FolderEntity): Long

    @Query("DELETE FROM folders WHERE name = :name")
    suspend fun deleteByName(name: String): Int
}

