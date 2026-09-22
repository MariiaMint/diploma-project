package frontend.navigation

import android.net.Uri

sealed class Route(val route: String) {

    object Main : Route("main")

    object Doctors : Route("doctors")

    object DoctorProfile : Route("doctor_profile") {
        private const val IS_NEW_ARG = "isNew"
        val withArgumentRoute = "$route?$IS_NEW_ARG={$IS_NEW_ARG}"
        fun createRoute(isNew: Boolean) = "$route?$IS_NEW_ARG=$isNew"
    }

    object Analyses : Route("analyses")

    object AnalysisFolder : Route("analyses_folder/{folderName}") {
        private const val FOLDER_NAME_ARG = "folderName"
        fun createRoute(folderName: String): String = "analyses_folder/${Uri.encode(folderName)}"
        val argName: String = FOLDER_NAME_ARG
    }

    object AnalysisProfile : Route("analysis_profile/{analysisId}") {
        private const val ANALYSIS_ID_ARG = "analysisId"
        fun createRoute(analysisId: String): String = "analysis_profile/${Uri.encode(analysisId)}"
        val argName: String = ANALYSIS_ID_ARG
    }

    object Conclusions : Route("conclusions")

    object ConclusionFolder : Route("conclusions_folder/{folderName}") {
        private const val FOLDER_NAME_ARG = "folderName"
        fun createRoute(folderName: String): String = "conclusions_folder/${Uri.encode(folderName)}"
        val argName: String = FOLDER_NAME_ARG
    }

    object ConclusionProfile : Route("conclusion_profile/{conclusionId}") {
        private const val CONCLUSION_ID_ARG = "conclusionId"
        fun createRoute(conclusionId: String): String = "conclusion_profile/${Uri.encode(conclusionId)}"
        val argName: String = CONCLUSION_ID_ARG
    }

    object Calendar : Route("calendar")
}