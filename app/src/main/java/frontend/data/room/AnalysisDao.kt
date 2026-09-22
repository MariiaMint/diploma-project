package frontend.data.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalysisDao {
    @Query("SELECT * FROM analyses ORDER BY date DESC")
    fun getAllFlow(): Flow<List<AnalysisEntity>>

    @Query("SELECT * FROM analyses WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): AnalysisEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AnalysisEntity): Long

    @Delete
    suspend fun delete(entity: AnalysisEntity): Int

    @Query("DELETE FROM analyses WHERE id = :id")
    suspend fun deleteById(id: String): Int
}


