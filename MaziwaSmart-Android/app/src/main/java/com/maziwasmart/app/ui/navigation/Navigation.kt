package com.maziwasmart.app.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.maziwasmart.app.ui.FarmViewModel
import com.maziwasmart.app.ui.aiscreens.AiMenuScreen
import com.maziwasmart.app.ui.aiscreens.InsightsScreen
import com.maziwasmart.app.ui.aiscreens.PredictionScreen
import com.maziwasmart.app.ui.aiscreens.RecommendationsScreen
import com.maziwasmart.app.ui.auth.AuthViewModel
import com.maziwasmart.app.ui.auth.LoginScreen
import com.maziwasmart.app.ui.auth.RegisterScreen
import com.maziwasmart.app.ui.cows.AddCowScreen
import com.maziwasmart.app.ui.cows.CowProfileScreen
import com.maziwasmart.app.ui.cows.CowsScreen
import com.maziwasmart.app.ui.dashboard.DashboardScreen
import com.maziwasmart.app.ui.finance.FinanceScreen
import com.maziwasmart.app.ui.records.FeedingScreen
import com.maziwasmart.app.ui.records.MilkScreen
import com.maziwasmart.app.ui.reports.ReportsScreen
import com.maziwasmart.app.ui.settings.SettingsScreen
import com.maziwasmart.app.ui.theme.Brown
import com.maziwasmart.app.ui.theme.Green
import com.maziwasmart.app.ui.theme.Muted

/** All the screens in the app */
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DASHBOARD = "dashboard"
    const val COWS = "cows"
    const val ADD_COW = "addCow"
    const val COW_PROFILE = "cow/{cowId}"
    const val FEEDING = "feeding?cowId={cowId}"
    const val MILK = "milk?cowId={cowId}"
    const val AI_MENU = "ai"
    const val PREDICTION = "prediction?cowId={cowId}"
    const val INSIGHTS = "insights"
    const val RECOMMENDATIONS = "recommendations"
    const val FINANCE = "finance"
    const val REPORTS = "reports"
    const val SETTINGS = "settings"

    fun cowProfile(cowId: String) = "cow/$cowId"
    fun feeding(cowId: String = "") = "feeding?cowId=$cowId"
    fun milk(cowId: String = "") = "milk?cowId=$cowId"
    fun prediction(cowId: String = "") = "prediction?cowId=$cowId"
}

private data class Tab(val label: String, val route: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("Home", Routes.DASHBOARD, Icons.Filled.Home),
    Tab("Cows", Routes.COWS, Icons.Filled.Pets),
    Tab("AI", Routes.AI_MENU, Icons.Filled.AutoAwesome),
    Tab("Advice", Routes.RECOMMENDATIONS, Icons.Filled.Lightbulb),
    Tab("Reports", Routes.REPORTS, Icons.Filled.BarChart)
)

@Composable
fun MaziwaApp() {
    val nav = rememberNavController()
    val authVm: AuthViewModel = viewModel()
    val farmVm: FarmViewModel = viewModel()

    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = tabs.any { it.route == currentRoute }
    // decided once when the app opens: logged in already -> Dashboard, otherwise -> Login
    val start = remember { if (authVm.isLoggedIn()) Routes.DASHBOARD else Routes.LOGIN }

    Scaffold(
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = { if (showBottomBar) BottomBar(nav, currentRoute) }
    ) { innerPadding ->
        NavHost(
            navController = nav,
            startDestination = start,
            modifier = Modifier.padding(innerPadding).then(if (showBottomBar) Modifier else Modifier.navigationBarsPadding())
        ) {
            val back: () -> Unit = { nav.popBackStack() }
            val go: (String) -> Unit = { route -> nav.navigate(route) }

            composable(Routes.LOGIN) {
                LoginScreen(
                    vm = authVm,
                    onLoggedIn = {
                        farmVm.refreshUser()
                        nav.navigate(Routes.DASHBOARD) { popUpTo(Routes.LOGIN) { inclusive = true } }
                    },
                    onRegister = { go(Routes.REGISTER) }
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(
                    vm = authVm,
                    onBack = back,
                    onRegistered = {
                        farmVm.refreshUser()
                        nav.navigate(Routes.DASHBOARD) { popUpTo(Routes.LOGIN) { inclusive = true } }
                    }
                )
            }
            composable(Routes.DASHBOARD) { DashboardScreen(farmVm, onNavigate = go) }
            composable(Routes.COWS) { CowsScreen(farmVm, onNavigate = go) }
            composable(Routes.ADD_COW) { AddCowScreen(farmVm, onBack = back) }
            composable(Routes.COW_PROFILE, arguments = listOf(navArgument("cowId") { type = NavType.StringType })) { entry ->
                CowProfileScreen(farmVm, cowId = entry.arguments?.getString("cowId") ?: "", onBack = back, onNavigate = go)
            }
            composable(Routes.FEEDING, arguments = listOf(optionalCowId())) { entry ->
                FeedingScreen(farmVm, startCowId = entry.arguments?.getString("cowId") ?: "", onBack = back,
                    onSaved = { cowId -> nav.navigate(Routes.milk(cowId)) })
            }
            composable(Routes.MILK, arguments = listOf(optionalCowId())) { entry ->
                MilkScreen(farmVm, startCowId = entry.arguments?.getString("cowId") ?: "", onBack = back,
                    onSaved = { cowId -> nav.navigate(Routes.prediction(cowId)) })
            }

            // ---- AI section (dummy data for now) ----
            composable(Routes.AI_MENU) { AiMenuScreen(onNavigate = go) }
            composable(Routes.PREDICTION, arguments = listOf(optionalCowId())) { entry ->
                PredictionScreen(farmVm, startCowId = entry.arguments?.getString("cowId") ?: "", onBack = back, onNavigate = go)
            }
            composable(Routes.INSIGHTS) { InsightsScreen(onBack = back, onNavigate = go) }
            composable(Routes.RECOMMENDATIONS) { RecommendationsScreen(onNavigate = go) }

            composable(Routes.FINANCE) { FinanceScreen(farmVm, onBack = back) }
            composable(Routes.REPORTS) { ReportsScreen(farmVm, onNavigate = go) }
            composable(Routes.SETTINGS) {
                SettingsScreen(farmVm, onBack = back, onLoggedOut = {
                    nav.navigate(Routes.LOGIN) { popUpTo(Routes.DASHBOARD) { inclusive = true } }
                })
            }
        }
    }
}

private fun optionalCowId() = navArgument("cowId") {
    type = NavType.StringType
    defaultValue = ""
}

@Composable
private fun BottomBar(nav: NavHostController, currentRoute: String?) {
    NavigationBar(containerColor = Color.White) {
        tabs.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = {
                    nav.navigate(tab.route) {
                        popUpTo(Routes.DASHBOARD) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Brown,
                    indicatorColor = Green,
                    unselectedIconColor = Muted,
                    unselectedTextColor = Muted
                )
            )
        }
    }
}
