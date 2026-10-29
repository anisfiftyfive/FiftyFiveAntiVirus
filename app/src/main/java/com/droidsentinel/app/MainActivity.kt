package com.droidsentinel.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.droidsentinel.app.ui.AppViewModel
import com.droidsentinel.app.ui.screens.AppDetailScreen
import com.droidsentinel.app.ui.screens.AppsScreen
import com.droidsentinel.app.ui.screens.HistoryScreen
import com.droidsentinel.app.ui.screens.HomeScreen
import com.droidsentinel.app.ui.screens.ResultsScreen
import com.droidsentinel.app.ui.screens.SettingsScreen
import com.droidsentinel.app.ui.theme.DroidSentinelTheme
import com.droidsentinel.app.ui.theme.LocalReduceMotion
import com.droidsentinel.app.ui.theme.Navy

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        setContent {
            val reduceMotion = android.provider.Settings.Global.getFloat(
                contentResolver,
                android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
                1f,
            ) == 0f
            CompositionLocalProvider(LocalReduceMotion provides reduceMotion) {
                DroidSentinelTheme {
                    SentinelApp()
                }
            }
        }
    }
}

private data class Dest(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
private fun SentinelApp(viewModel: AppViewModel = viewModel()) {
    val nav = rememberNavController()
    val home by viewModel.home.collectAsStateWithLifecycle()
    val apps by viewModel.apps.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val selected by viewModel.selectedApp.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) viewModel.scanApk(uri)
    }
    val tabs = listOf(
        Dest("home", "Home", Icons.Outlined.Home),
        Dest("apps", "Apps", Icons.Outlined.Apps),
        Dest("history", "History", Icons.Outlined.History),
        Dest("settings", "Settings", Icons.Outlined.Settings),
    )
    Scaffold(
        containerColor = Navy,
        bottomBar = {
            val route = nav.currentBackStackEntryAsState().value?.destination?.route
            NavigationBar(containerColor = Navy) {
                tabs.forEach { dest ->
                    NavigationBarItem(
                        selected = route == dest.route,
                        onClick = {
                            nav.navigate(dest.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(dest.icon, dest.label) },
                        label = { Text(dest.label) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = "home",
            modifier = Modifier.padding(padding),
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() },
        ) {
            composable("home") {
                HomeScreen(
                    state = home,
                    onScanApps = viewModel::scanDevice,
                    onScanApk = { picker.launch(arrayOf("application/vnd.android.package-archive", "application/octet-stream", "*/*")) },
                    onCancel = viewModel::cancelScan,
                    onOpenResults = { nav.navigate("results") },
                    onOpenSettings = {
                        nav.navigate("settings") {
                            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            composable("apps") {
                AppsScreen(
                    apps = apps,
                    lastResult = home.lastResult,
                    query = query,
                    onQuery = viewModel::setQuery,
                    onOpen = { pkg ->
                        viewModel.openApp(pkg)
                        nav.navigate("appDetail")
                    },
                )
            }
            composable("appDetail") {
                AppDetailScreen(selected, viewModel.explainSelected())
            }
            composable("results") {
                ResultsScreen(home.lastResult, home.advisor)
            }
            composable("history") {
                HistoryScreen(history)
            }
            composable("settings") {
                SettingsScreen(home, viewModel::updateDatabase)
            }
        }
    }
}
