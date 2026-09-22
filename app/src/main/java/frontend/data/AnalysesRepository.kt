package frontend.data

import android.content.Context
import frontend.data.room.AnalysisEntity
import frontend.data.room.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class AnalysisModel(val id: String, val title: String, val date: String)

class AnalysesRepository(private val context: Context) {
    private val dao = AppDatabase.getInstance(context).analysisDao()

    fun getAllFlow(): Flow<List<AnalysisModel>> = dao.getAllFlow().map { list ->
        list.map { AnalysisModel(it.id, it.title, it.date) }
    }

    suspend fun getById(id: String): AnalysisModel? {
        return dao.getById(id)?.let { AnalysisModel(it.id, it.title, it.date) }
    }

    suspend fun insert(model: AnalysisModel) {
        dao.insert(AnalysisEntity(id = model.id, title = model.title, date = model.date))
    }

    suspend fun delete(id: String) { dao.deleteById(id) }
}

