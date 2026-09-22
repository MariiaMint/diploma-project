package frontend.modules.conclusions

import android.graphics.Bitmap

enum class ConclusionSourceType {
    DOCTOR,
    CLINIC,
    UNKNOWN
}

data class ConclusionFileAttachment(
    val uri: String,
    val displayName: String
)

data class ConclusionDraft(
    val title: String,
    val date: String,
    val sourceType: ConclusionSourceType,
    val doctorId: String?,
    val doctorName: String?,
    val doctorSpecialization: String?,
    val clinicName: String?,
    val sourceNote: String,
    val folders: List<String>,
    val photos: List<Bitmap>,
    val files: List<ConclusionFileAttachment>
)

