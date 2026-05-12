package ir.moha.persiantodo.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ir.moha.persiantodo.ui.screens.*

sealed class Screen(val route: String) {
    object Home     : Screen("home")
    object AddTask  : Screen("add_task")
    object EditTask : Screen("edit_task/{taskId}") {
        fun createRoute(taskId: Long) = "edit_task/$taskId"
    }
    object Search   : Screen("search")
    object Stats    : Screen("stats")
    object Settings : Screen("settings")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    isDarkMode: Boolean,
    isDynamicColor: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    onToggleDynamicColor: (Boolean) -> Unit
) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {

        composable(Screen.Home.route) {
            HomeScreen(
                onAddTask  = { navController.navigate(Screen.AddTask.route) },
                onEditTask = { id -> navController.navigate(Screen.EditTask.createRoute(id)) }
            )
        }

        composable(Screen.AddTask.route) {
            AddEditTaskScreen(
                taskId = null,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditTask.route,
            arguments = listOf(navArgument("taskId") { type = NavType.LongType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getLong("taskId")
            AddEditTaskScreen(
                taskId = taskId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onBack     = { navController.popBackStack() },
                onEditTask = { id -> navController.navigate(Screen.EditTask.createRoute(id)) }
            )
        }

        composable(Screen.Stats.route) {
            StatsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack             = { navController.popBackStack() },
                isDarkMode         = isDarkMode,
                isDynamicColor     = isDynamicColor,
                onToggleDarkMode   = onToggleDarkMode,
                onToggleDynamicColor = onToggleDynamicColor
            )
        }
    }
}
