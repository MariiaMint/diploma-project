package frontend.data

import android.content.Context
import frontend.data.room.AppDatabase
import frontend.data.room.FolderEntity
import frontend.data.room.FolderLinkEntity
import java.util.*

data class FolderModel(val name: String)

class FoldersRepository(private val context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val folderDao = db.folderDao()
    private val linkDao = db.folderLinkDao()

    suspend fun insertFolder(name: String) {
        folderDao.insert(FolderEntity(name = name))
    }

    fun getAllFolders(): List<FolderModel> = folderDao.getAll().map { FolderModel(it.name) }

    fun getLinksForFolder(folderName: String): List<String> = linkDao.getLinksForFolder(folderName).map { it.analysisId }

    suspend fun linkAnalysisToFolder(analysisId: String, folderName: String) {
        val id = UUID.randomUUID().toString()
        linkDao.insert(FolderLinkEntity(id = id, folderName = folderName, analysisId = analysisId))
    }

    fun getLinksForAnalysis(analysisId: String): List<String> = linkDao.getLinksForAnalysis(analysisId).map { it.folderName }

}



