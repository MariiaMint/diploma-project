package frontend.data.storage

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import java.io.File

class FileStorage(private val context: Context) {
    private val attachmentsDir: File = File(context.filesDir, "attachments").apply { if (!exists()) mkdirs() }

    private fun getMasterKey(): MasterKey {
        return MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    fun saveEncrypted(name: String, bytes: ByteArray): String {
        val file = File(attachmentsDir, name)
        val encryptedFile = EncryptedFile.Builder(
            context,
            file,
            getMasterKey(),
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
        encryptedFile.openFileOutput().use { it.write(bytes) }
        return file.absolutePath
    }

    fun readEncrypted(path: String): ByteArray? {
        val file = File(path)
        if (!file.exists()) return null
        val encryptedFile = EncryptedFile.Builder(
            context,
            file,
            getMasterKey(),
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
        return encryptedFile.openFileInput().use { it.readBytes() }
    }

    fun delete(path: String): Boolean {
        val file = File(path)
        return if (file.exists()) file.delete() else false
    }
}

