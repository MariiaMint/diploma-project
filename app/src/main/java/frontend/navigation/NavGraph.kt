package frontend.navigation

import android.net.Uri

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import frontend.modules.MainScreen
import frontend.modules.analyses.AnalysisFolderScreen
import frontend.modules.analyses.AnalysisProfileScreen
import frontend.modules.analyses.AnalysesScreen
// removed direct AnalysesStore import — using rememberAnalysesStore()
import frontend.modules.calendar.CalendarScreen
import frontend.modules.conclusions.ConclusionsScreen
import frontend.modules.doctors.DoctorProfileScreen
import frontend.modules.doctors.DoctorsScreen
import frontend.modules.bars.BottomBar
import frontend.modules.bars.HeaderBar
import frontend.modules.BackgroundScreen
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import frontend.modules.analyses.rememberAnalysesStore
import frontend.modules.conclusions.ConclusionFolderScreen
import frontend.modules.conclusions.ConclusionProfileScreen
import frontend.modules.conclusions.ConclusionsStore

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val analysesStore = rememberAnalysesStore()
    val conclusionsStore = remember { ConclusionsStore() }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    BackgroundScreen {
        Column(modifier = Modifier.fillMaxSize()) {

            HeaderBar(navController, currentRoute)

            NavHost(
                navController = navController,
                startDestination = Route.Main.route,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {

                composable(Route.Main.route) {
                    MainScreen(navController)
                }

                composable(Route.Doctors.route) {
                    DoctorsScreen(navController)
                }

                composable(
                    route = Route.DoctorProfile.withArgumentRoute,
                    arguments = listOf(
                        navArgument("isNew") {
                            type = NavType.BoolType
                            defaultValue = false
                        }
                    )
                ) { backStackEntry ->
                    val isNewDoctor = backStackEntry.arguments?.getBoolean("isNew") ?: false
                    DoctorProfileScreen(navController, isNewDoctor = isNewDoctor)
                }

                composable(Route.Analyses.route) {
                    AnalysesScreen(navController, analysesStore)
                }

                composable(
                    route = Route.AnalysisFolder.route,
                    arguments = listOf(
                        navArgument(Route.AnalysisFolder.argName) {
                            type = NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    val folderName = Uri.decode(
                        backStackEntry.arguments?.getString(Route.AnalysisFolder.argName).orEmpty()
                    )
                    AnalysisFolderScreen(
                        navController = navController,
                        analysesStore = analysesStore,
                        initialFolderName = folderName
                    )
                }

                composable(
                    route = Route.AnalysisProfile.route,
                    arguments = listOf(
                        navArgument(Route.AnalysisProfile.argName) {
                            type = NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    val analysisId = Uri.decode(
                        backStackEntry.arguments?.getString(Route.AnalysisProfile.argName).orEmpty()
                    )
                    AnalysisProfileScreen(
                        navController = navController,
                        analysesStore = analysesStore,
                        analysisId = analysisId
                    )
                }

                composable(Route.Conclusions.route) {
                    ConclusionsScreen(navController, conclusionsStore)
                }

                composable(
                    route = Route.ConclusionFolder.route,
                    arguments = listOf(
                        navArgument(Route.ConclusionFolder.argName) {
                            type = NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    val folderName = Uri.decode(
                        backStackEntry.arguments?.getString(Route.ConclusionFolder.argName)
                            .orEmpty()
                    )
                    ConclusionFolderScreen(
                        navController = navController,
                        conclusionsStore = conclusionsStore,
                        initialFolderName = folderName
                    )
                }

                composable(
                    route = Route.ConclusionProfile.route,
                    arguments = listOf(
                        navArgument(Route.ConclusionProfile.argName) {
                            type = NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    val conclusionId = Uri.decode(
                        backStackEntry.arguments?.getString(Route.ConclusionProfile.argName)
                            .orEmpty()
                    )
                    ConclusionProfileScreen(
                        navController = navController,
                        conclusionsStore = conclusionsStore,
                        conclusionId = conclusionId
                    )
                }

                composable(Route.Calendar.route) {
                    CalendarScreen(navController)
                }
            }

            BottomBar(navController, currentRoute)
        }
    }
}