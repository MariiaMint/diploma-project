package frontend.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.security.crypto.MasterKey
import net.sqlcipher.database.SupportFactory
import java.security.SecureRandom
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

@Database(
    entities = [
        AnalysisEntity::class,
        AttachmentEntity::class,
        FolderEntity::class,
        FolderLinkEntity::class
    ],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun analysisDao(): AnalysisDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun folderDao(): FolderDao
    abstract fun folderLinkDao(): FolderLinkDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val DB_NAME = "mymedbook_db"
        private const val PREFS_NAME = "mymedbook_prefs"
        private const val PREF_PASSPHRASE = "db_passphrase"

        private fun getOrCreatePassphrase(context: Context): ByteArray {
            // Use EncryptedSharedPreferences to store the SQLCipher passphrase securely
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val prefs = EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            var base64 = prefs.getString(PREF_PASSPHRASE, null)
            if (base64 == null) {
                val bytes = ByteArray(32)
                SecureRandom().nextBytes(bytes)
                base64 = Base64.encodeToString(bytes, Base64.DEFAULT)
                prefs.edit().putString(PREF_PASSPHRASE, base64).apply()
            }
            return Base64.decode(base64, Base64.DEFAULT)
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val passphrase = getOrCreatePassphrase(context)
                val factory = SupportFactory(passphrase)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                ).openHelperFactory(factory)
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// Migration from version 1 -> 2: create attachments table
val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // create attachments table
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS attachments (
                id TEXT NOT NULL PRIMARY KEY,
                owner_id TEXT NOT NULL,
                owner_type TEXT NOT NULL,
                file_path TEXT NOT NULL,
                file_name TEXT NOT NULL,
                mime_type TEXT,
                created_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

// Migration from version 2 -> 3: create folders and folder_links tables
val MIGRATION_2_3: Migration = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS folders (
                name TEXT NOT NULL PRIMARY KEY
            )
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS folder_links (
                id TEXT NOT NULL PRIMARY KEY,
                folder_name TEXT NOT NULL,
                analysis_id TEXT NOT NULL
            )
            """.trimIndent()
        )
    }
}

