package ir.moha.persiantodo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.preferences.core.edit
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import ir.moha.persiantodo.navigation.NavGraph
import ir.moha.persiantodo.navigation.Screen
import ir.moha.persiantodo.ui.screens.DARK_MODE_KEY
import ir.moha.persiantodo.ui.screens.DYNAMIC_COLOR_KEY
import ir.moha.persiantodo.ui.screens.dataStore
import ir.moha.persiantodo.ui.theme.PersianTodoTheme
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current
            val scope   = rememberCoroutineScope()

            val isDarkMode by context.dataStore.data
                .map { it[DARK_MODE_KEY] ?: false }
                .collectAsState(initial = false)

            val isDynamicColor by context.dataStore.data
                .map { it[DYNAMIC_COLOR_KEY] ?: true }
                .collectAsState(initial = true)

            PersianTodoTheme(darkTheme = isDarkMode, dynamicColor = isDynamicColor) {
                val navController = rememberNavController()
                val navBackStack  by navController.currentBackStackEntryAsState()
                val currentRoute  = navBackStack?.destination?.route

                val bottomBarRoutes = listOf(Screen.Home.route, Screen.Stats.route)
                val showBottomBar   = currentRoute in bottomBarRoutes

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = currentRoute == Screen.Home.route,
                                    onClick  = {
                                        navController.navigate(Screen.Home.route) {
                                            popUpTo(Screen.Home.route) { inclusive = true }
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            if (currentRoute == Screen.Home.route) Icons.Filled.Home else Icons.Outlined.Home,
                                            contentDescription = "خانه"
                                        )
                                    },
                                    label = { Text("خانه") }
                                )
                                NavigationBarItem(
                                    selected = false,
                                    onClick  = { navController.navigate(Screen.Search.route) },
                                    icon     = { Icon(Icons.Outlined.Search, "جستجو") },
                                    label    = { Text("جستجو") }
                                )
                                NavigationBarItem(
                                    selected = currentRoute == Screen.Stats.route,
                                    onClick  = { navController.navigate(Screen.Stats.route) },
                                    icon = {
                                        Icon(
                                            if (currentRoute == Screen.Stats.route) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                                            contentDescription = "آمار"
                                        )
                                    },
                                    label = { Text("آمار") }
                                )
                                NavigationBarItem(
                                    selected = currentRoute == Screen.Settings.route,
                                    onClick  = { navController.navigate(Screen.Settings.route) },
                                    icon = {
                                        Icon(
                                            if (currentRoute == Screen.Settings.route) Icons.Filled.Settings else Icons.Outlined.Settings,
                                            contentDescription = "تنظیمات"
                                        )
                                    },
                                    label = { Text("تنظیمات") }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavGraph(
                        navController        = navController,
                        isDarkMode           = isDarkMode,
                        isDynamicColor       = isDynamicColor,
                        onToggleDarkMode     = { v ->
                            scope.launch { context.dataStore.edit { it[DARK_MODE_KEY] = v } }
                        },
                        onToggleDynamicColor = { v ->
                            scope.launch { context.dataStore.edit { it[DYNAMIC_COLOR_KEY] = v } }
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
