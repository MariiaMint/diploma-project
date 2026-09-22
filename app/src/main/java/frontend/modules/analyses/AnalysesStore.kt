package frontend.modules.analyses

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import frontend.data.AnalysesRepository
import frontend.modules.common.mapMedicalIconByKeywords
import frontend.data.FoldersRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AnalysisFileAttachment(
    val uri: String,
    val displayName: String
)

data class AnalysisItem(
    val id: String,
    val title: String,
    val date: String
)

class AnalysesStore {
    // no built-in sample data — store is empty and will be populated from DB via rememberAnalysesStore
    val folderNames = mutableStateListOf<String>()

    private var nextId = 1L

    val analyses = mutableStateListOf<AnalysisItem>()

    // folder -> list of analysis ids
    val folderAnalyses = mutableStateMapOf<String, MutableList<String>>()

    val folderIcons = mutableStateMapOf<String, Int>()

    val analysisIcons = mutableStateMapOf<String, Int>()

    val analysisPhotos = mutableStateMapOf<String, MutableList<Bitmap>>()
    val analysisFiles = mutableStateMapOf<String, MutableList<AnalysisFileAttachment>>()

    private fun generateId(): String {
        val id = "analysis_${nextId}"
        nextId += 1
        return id
    }

    private fun findAnalysisById(id: String): AnalysisItem? = analyses.firstOrNull { it.id == id }

    fun getAnalysisById(id: String): AnalysisItem? = findAnalysisById(id)

    fun getAllAnalyses(): List<AnalysisItem> = analyses.toList()

    fun foldersForAnalysis(analysisId: String): List<String> {
        return folderAnalyses.entries
            .filter { (_, analysisIds) -> analysisIds.contains(analysisId) }
            .map { (folderName, _) -> folderName }
    }

    fun photosForAnalysis(analysisId: String): List<Bitmap> = analysisPhotos[analysisId].orEmpty()

    fun filesForAnalysis(analysisId: String): List<AnalysisFileAttachment> = analysisFiles[analysisId].orEmpty()

    fun addFolder(name: String): Boolean {
        if (name.isBlank() || folderNames.any { it.equals(name, ignoreCase = true) }) return false
        folderNames.add(name)
        folderAnalyses[name] = mutableStateListOf()
        folderIcons[name] = mapMedicalIconByKeywords(name)
        return true
    }

    fun renameFolder(oldName: String, newName: String): Boolean {
        if (newName.isBlank()) return false
        if (folderNames.any { it.equals(newName, ignoreCase = true) && !it.equals(oldName, ignoreCase = true) }) return false

        val index = folderNames.indexOfFirst { it.equals(oldName, ignoreCase = true) }
        if (index == -1) return false

        val actualOldName = folderNames[index]
        folderNames[index] = newName

        val links = folderAnalyses.remove(actualOldName) ?: mutableStateListOf()
        folderAnalyses[newName] = links

        val icon = folderIcons.remove(actualOldName) ?: mapMedicalIconByKeywords(newName)
        folderIcons[newName] = icon
        return true
    }

    fun deleteFolder(name: String): Boolean {
        val actualName = folderNames.firstOrNull { it.equals(name, ignoreCase = true) } ?: return false
        folderNames.remove(actualName)
        folderAnalyses.remove(actualName)
        folderIcons.remove(actualName)
        return true
    }

    fun addNewAnalysisToFolder(folderName: String, title: String, date: String): Boolean {
        if (title.isBlank() || date.isBlank()) return false

        val actualFolderName = folderNames.firstOrNull { it.equals(folderName, ignoreCase = true) } ?: return false
        val id = generateId()

        analyses.add(AnalysisItem(id = id, title = title, date = date))
        analysisIcons[id] = mapMedicalIconByKeywords(title)

        return linkExistingAnalysisToFolder(actualFolderName, id)
    }

    fun linkExistingAnalysisToFolder(folderName: String, analysisId: String): Boolean {
        val actualFolderName = folderNames.firstOrNull { it.equals(folderName, ignoreCase = true) } ?: return false
        val actualId = analyses.firstOrNull { it.id == analysisId }?.id ?: return false

        val links = folderAnalyses.getOrPut(actualFolderName) { mutableStateListOf() }
        if (links.any { it == actualId }) return false

        links.add(actualId)
        return true
    }

    fun analysesForFolder(folderName: String): List<AnalysisItem> {
        val actualFolderName = folderNames.firstOrNull { it.equals(folderName, ignoreCase = true) } ?: return emptyList()
        val links = folderAnalyses[actualFolderName].orEmpty()
        return links.mapNotNull { id -> findAnalysisById(id) }
    }

    /**
     * Добавляет анализ в in-memory store и возвращает сгенерированный id (или null при ошибке).
     * Persist в БД выполняется извне (в репозитории) — store больше не самозасевает БД.
     */
    fun addAnalysis(
        title: String,
        date: String,
        folders: List<String> = emptyList(),
        photos: List<Bitmap> = emptyList(),
        files: List<AnalysisFileAttachment> = emptyList()
    ): String? {
        if (title.isBlank() || date.isBlank()) return null

        val id = generateId()
        analyses.add(AnalysisItem(id = id, title = title, date = date))
        analysisIcons[id] = mapMedicalIconByKeywords(title)

        if (photos.isNotEmpty()) {
            val existingPhotos = analysisPhotos.getOrPut(id) { mutableStateListOf() }
            existingPhotos.addAll(photos)
        }

        if (files.isNotEmpty()) {
            val existingFiles = analysisFiles.getOrPut(id) { mutableStateListOf() }
            existingFiles.addAll(files)
        }

        folders.forEach { folderName ->
            linkExistingAnalysisToFolder(folderName, id)
        }

        return id
    }

    fun deleteAnalysis(id: String): Boolean {
        val index = analyses.indexOfFirst { it.id == id }
        if (index == -1) return false

        analyses.removeAt(index)
        analysisIcons.remove(id)
        analysisPhotos.remove(id)
        analysisFiles.remove(id)

        // remove links from folders
        folderAnalyses.values.forEach { list -> list.remove(id) }

        return true
    }

    fun updateAnalysisTitle(id: String, newTitle: String): Boolean {
        val analysisIndex = analyses.indexOfFirst { it.id == id }
        if (analysisIndex == -1 || newTitle.isBlank()) return false

        val updatedAnalysis = analyses[analysisIndex].copy(title = newTitle)
        analyses[analysisIndex] = updatedAnalysis
        analysisIcons[id] = mapMedicalIconByKeywords(newTitle)
        return true
    }
}


@Composable
fun rememberAnalysesStore(): AnalysesStore {
    val context = LocalContext.current
    val repo = AnalysesRepository(context)
    val store = remember { AnalysesStore() }

    // sync DB -> in-memory store (no automatic seeding)
    LaunchedEffect(Unit) {
        repo.getAllFlow().collect { list ->

            store.analyses.clear()
            // clear attachments and folder links to rebuild
            store.analysisPhotos.clear()
            store.analysisFiles.clear()
            store.folderAnalyses.clear()

            // for each analysis load attachments and folder links
            list.forEach { m ->
                store.analyses.add(AnalysisItem(id = m.id, title = m.title, date = m.date))
            }

            // load attachments and links in background
            withContext(Dispatchers.IO) {
                val attachmentsRepo = frontend.data.AttachmentsRepository(context)
                val foldersRepo = FoldersRepository(context)

                list.forEach { m ->
                    try {
                        val (photos, files) = attachmentsRepo.loadForAnalysis(m.id)
                        if (photos.isNotEmpty()) store.analysisPhotos[m.id] = photos.toMutableList()
                        if (files.isNotEmpty()) store.analysisFiles[m.id] = files.toMutableList()
                    } catch (_: Exception) {
                        // ignore per-item errors
                    }

                    try {
                        val links = foldersRepo.getLinksForAnalysis(m.id)
                        links.forEach { folderName ->
                            val listFor = store.folderAnalyses.getOrPut(folderName) { mutableStateListOf() }
                            listFor.add(m.id)
                        }
                    } catch (_: Exception) {
                        // ignore
                    }
                }
            }
        }
    }

    // load folders from DB into store
    LaunchedEffect(Unit) {
        val folders = withContext(Dispatchers.IO) {
            try {
                FoldersRepository(context).getAllFolders()
            } catch (_: Exception) {
                emptyList()
            }
        }

        store.folderNames.clear()
        folders.forEach { store.folderNames.add(it.name) }
    }

    return store
}
