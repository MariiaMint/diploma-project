package frontend.modules.conclusions

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import frontend.modules.common.mapMedicalIconByKeywords
import java.util.UUID

data class ConclusionModel(
	val id: String,
	var title: String,
	var date: String,
	var sourceType: ConclusionSourceType,
	var doctorId: String?,
	var doctorName: String?,
	var doctorSpecialization: String?,
	var clinicName: String?,
	var sourceNote: String,
	var folders: List<String>,
	var photos: List<Bitmap>,
	var files: List<ConclusionFileAttachment>
)

class ConclusionsStore {
	val conclusions: SnapshotStateList<ConclusionModel> = mutableStateListOf()
	val knownClinics = mutableStateListOf<String>()
	val folderIcons = mutableStateMapOf<String, Int>()
	val conclusionIcons = mutableStateMapOf<String, Int>()

	init {
		// начальные данные, похожие на прежние
		val sample = listOf(
			ConclusionModel(
				id = "conclusion_1",
				title = "Заключение кардиолог Пщенко В.В.",
				date = "12.05.2024",
				sourceType = ConclusionSourceType.DOCTOR,
				doctorId = "doc_1",
				doctorName = "Пщенко В.В.",
                doctorSpecialization = null,
				clinicName = null,
				sourceNote = "",
				folders = listOf("Кардиологи"),
				photos = emptyList(),
				files = emptyList()
			),
			ConclusionModel(
				id = "conclusion_2",
				title = "Заключение уролог Иванов И.И.",
				date = "15.08.2024",
				sourceType = ConclusionSourceType.CLINIC,
				doctorId = null,
				doctorName = null,
                doctorSpecialization = null,
				clinicName = "Клиника Здоровье",
				sourceNote = "",
				folders = listOf("Урологи"),
				photos = emptyList(),
				files = emptyList()
			),
			ConclusionModel(
				id = "conclusion_3",
				title = "Заключение хирург Леонов К.К.",
				date = "20.09.2024",
				sourceType = ConclusionSourceType.UNKNOWN,
				doctorId = null,
				doctorName = null,
                doctorSpecialization = null,
				clinicName = null,
				sourceNote = "Архивная запись",
				folders = listOf("Хирург Петров А.А."),
				photos = emptyList(),
				files = emptyList()
			)
		)

		conclusions.addAll(sample)
		knownClinics.addAll(listOf("Клиника Здоровье", "Медцентр Плюс", "Поликлиника N1"))
		// icons will be filled by consumer using mapMedicalIconByKeywords
		conclusions.forEach { conclusionIcons[it.id] = mapMedicalIconByKeywords(it.title) }
	}

	fun getById(id: String): ConclusionModel? = conclusions.find { it.id == id }

	fun addFromDraft(draft: ConclusionDraft): String {
		val id = UUID.randomUUID().toString()
		conclusions.add(
			ConclusionModel(
				id = id,
				title = draft.title,
				date = draft.date,
				sourceType = draft.sourceType,
				doctorId = draft.doctorId,
				doctorName = draft.doctorName,
                doctorSpecialization = draft.doctorSpecialization,
				clinicName = draft.clinicName,
				sourceNote = draft.sourceNote,
				folders = draft.folders,
				photos = draft.photos,
				files = draft.files
			)
		)
		conclusionIcons[id] = mapMedicalIconByKeywords(draft.title)
		draft.clinicName?.trim()?.takeIf { it.isNotEmpty() }?.let { if (knownClinics.none { k -> k.equals(it, ignoreCase = true) }) knownClinics.add(it) }
		return id
	}

	fun updateFromDraft(id: String, draft: ConclusionDraft): Boolean {
		val idx = conclusions.indexOfFirst { it.id == id }
		if (idx == -1) return false
		conclusions[idx] = conclusions[idx].copy(
			title = draft.title,
			date = draft.date,
			sourceType = draft.sourceType,
			doctorId = draft.doctorId,
			doctorName = draft.doctorName,
                doctorSpecialization = draft.doctorSpecialization,
			clinicName = draft.clinicName,
			sourceNote = draft.sourceNote,
			folders = draft.folders,
			photos = draft.photos,
			files = draft.files
		)
		conclusionIcons[id] = mapMedicalIconByKeywords(draft.title)
		return true
	}

	fun delete(id: String): Boolean = conclusions.removeIf { it.id == id }

}
