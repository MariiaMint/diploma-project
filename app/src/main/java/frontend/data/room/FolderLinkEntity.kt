package frontend.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folder_links")
data class FolderLinkEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "folder_name")
    val folderName: String,
    @ColumnInfo(name = "analysis_id")
    val analysisId: String
)

