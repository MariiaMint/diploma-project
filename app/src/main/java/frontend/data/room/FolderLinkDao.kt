package frontend.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FolderLinkDao {
    @Query("SELECT * FROM folder_links WHERE folder_name = :folderName ORDER BY analysis_id")
    fun getLinksForFolder(folderName: String): List<FolderLinkEntity>

    @Query("SELECT * FROM folder_links WHERE analysis_id = :analysisId")
    fun getLinksForAnalysis(analysisId: String): List<FolderLinkEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: FolderLinkEntity): Long

    @Query("DELETE FROM folder_links WHERE folder_name = :folderName AND analysis_id = :analysisId")
    suspend fun deleteLink(folderName: String, analysisId: String): Int
}

