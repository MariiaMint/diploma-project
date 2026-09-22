package frontend.data

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import frontend.data.room.AttachmentEntity
import frontend.data.room.AppDatabase
import frontend.data.storage.FileStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import java.io.ByteArrayOutputStream
import java.util.*
import android.graphics.BitmapFactory
import frontend.modules.analyses.AnalysisFileAttachment

/**
 * Репозиторий для сохранения/чтения вложений (файлов и фото).
 * Сохраняет файлы зашифрованными через FileStorage и добавляет записи в attachments.
 */
class AttachmentsRepository(private val context: Context) {
    private val dao = AppDatabase.getInstance(context).attachmentDao()
    private val storage = FileStorage(context)

    /**
     * Save provided photos and files for given analysisId.
     * Returns list of attachment ids that were created.
     */
    suspend fun saveForAnalysis(
        analysisId: String,
        photos: List<Bitmap>,
        files: List<frontend.modules.analyses.AnalysisFileAttachment>
    ): List<String> = withContext(Dispatchers.IO) {
        val createdIds = mutableListOf<String>()
        // Сохраняем фото-битмапы как JPEG
        photos.forEach { bitmap ->
            try {
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos)
                val bytes = baos.toByteArray()
                val fileName = "photo_${UUID.randomUUID()}.jpg"
                val path = storage.saveEncrypted(fileName, bytes)

                val id = UUID.randomUUID().toString()
                val entity = AttachmentEntity(
                    id = id,
                    ownerId = analysisId,
                    ownerType = "analysis",
                    filePath = path,
                    fileName = fileName,
                    mimeType = "image/jpeg",
                    createdAt = System.currentTimeMillis()
                )
                dao.insert(entity)
                createdIds.add(id)
            } catch (e: Exception) {
                Log.e("AttachmentsRepo", "Failed to save photo attachment", e)
            }
        }

        // Сохраняем выбранные файлы (URI -> байты -> encrypted file)
        files.forEach { file ->
            try {
                val uri = Uri.parse(file.uri)
                val input = context.contentResolver.openInputStream(uri)
                val bytes = input?.readBytes()
                input?.close()
                if (bytes == null) return@forEach

                val safeName = file.displayName.ifBlank { "file_${UUID.randomUUID()}" }
                val path = storage.saveEncrypted("${analysisId}_${UUID.randomUUID()}_${safeName}", bytes)

                val mime = try {
                    context.contentResolver.getType(uri)
                } catch (_: Exception) {
                    null
                }

                val id = UUID.randomUUID().toString()
                val entity = AttachmentEntity(
                    id = id,
                    ownerId = analysisId,
                    ownerType = "analysis",
                    filePath = path,
                    fileName = file.displayName,
                    mimeType = mime,
                    createdAt = System.currentTimeMillis()
                )
                dao.insert(entity)
                createdIds.add(id)
            } catch (e: Exception) {
                Log.e("AttachmentsRepo", "Failed to save file attachment: ${file.displayName}", e)
            }
        }
        createdIds.toList()
    }

    suspend fun loadForAnalysis(analysisId: String): Pair<List<Bitmap>, List<AnalysisFileAttachment>> =
        withContext(Dispatchers.IO) {
            val list = dao.getForOwner(analysisId, "analysis")
            val photos = mutableListOf<Bitmap>()
            val filesOut = mutableListOf<AnalysisFileAttachment>()

            list.forEach { entity ->
                try {
                    val bytes = storage.readEncrypted(entity.filePath) ?: return@forEach
                    val mime = entity.mimeType
                    if (mime != null && mime.startsWith("image") ) {
                        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        if (bmp != null) photos.add(bmp)
                    } else {
                        filesOut.add(AnalysisFileAttachment(uri = entity.filePath, displayName = entity.fileName))
                    }
                } catch (e: Exception) {
                    Log.e("AttachmentsRepo", "Failed to load attachment id=${entity.id}", e)
                }
            }

            Pair(photos, filesOut)
        }
}


