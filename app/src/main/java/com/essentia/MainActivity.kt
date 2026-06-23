package com.essentia

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.essentia.crash.CrashLogger
import com.essentia.ui.components.BottomNav
import com.essentia.ui.components.MainTab
import com.essentia.ui.diagnostics.DiagnosticsScreen
import com.essentia.ui.navigation.Routes
import com.essentia.ui.navigation.mainGraph
import com.essentia.ui.navigation.onboardingGraph
import com.essentia.ui.theme.EssentiaTheme
import com.essentia.ui.theme.PaperWhite
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate start, action=${intent?.action}")
        runCatching { enableEdgeToEdge() }
            .onFailure { Log.e(TAG, "enableEdgeToEdge failed", it) }

        val openDiagnostics = intent?.action == ACTION_DIAGNOSTICS
        if (openDiagnostics) {
            Log.i(TAG, "Launching directly into Diagnostics (recover mode)")
            showDiagnostics()
        } else {
            Log.i(TAG, "Launching AppRoot")
            runCatching { showAppRoot() }
                .onFailure { Log.e(TAG, "AppRoot setContent failed", it) }
        }
        Log.i(TAG, "onCreate done")
    }

    private fun showAppRoot() {
        setContent {
            EssentiaTheme {
                AppRoot(onOpenDiagnostics = ::showDiagnostics)
            }
        }
    }

    /**
     * Replaces the composable content with a Diagnostics screen so the user can
     * see crash logs even if the main UI is broken. Triggered from inside the app
     * (Settings → Diagnostics) or from a launcher intent with action DIAGNOSTICS.
     */
    private fun showDiagnostics() {
        setContent {
            EssentiaTheme {
                DiagnosticsScreen()
            }
        }
    }

    companion object {
        private const val TAG = "EssentiaMain"
        const val ACTION_DIAGNOSTICS = "com.essentia.action.DIAGNOSTICS"
    }
}

/**
 * Resolves whether the user has finished first-run onboarding and routes them to
 * Onboarding or the main graph accordingly.
 */
@Composable
private fun AppRoot(onOpenDiagnostics: () -> Unit) {
    val navController = rememberNavController()
    AppModule.navController = navController

    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        // Resolve start destination based on whether the user is onboarded.
        val onboarded = runCatching {
            AppModule.userPreferences.onboarded.first()
        }.getOrDefault(false)
        startDestination = if (onboarded) Routes.MAIN else Routes.ONBOARDING
    }

    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val isOnboarding = currentRoute == Routes.ONBOARDING || currentRoute == Routes.LOADING
    val showBottomNav = !isOnboarding

    val currentTab = when (currentRoute) {
        Routes.EDITIONS -> MainTab.Editions
        Routes.ARCHIVE -> MainTab.Archive
        else -> MainTab.Today
    }

    val resolved = startDestination
    if (resolved == null) {
        // Brief loading state while we resolve the start destination from DataStore.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PaperWhite)
        )
        return
    }

    Scaffold(
        containerColor = PaperWhite,
        contentColor = Color.Black,
        bottomBar = {
            if (showBottomNav) {
                BottomNav(
                    current = currentTab,
                    onSelect = { tab ->
                        if (tab.route != currentRoute) {
                            runCatching {
                                navController.navigate(tab.route) {
                                    popUpTo(Routes.MAIN) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(PaperWhite)
        ) {
            NavHost(
                navController = navController,
                startDestination = resolved,
                modifier = Modifier.fillMaxSize()
            ) {
                mainGraph(
                    onOpenItem = { id -> runCatching { navController.navigate(Routes.detail(id)) } },
                    onOpenEdition = { id -> runCatching { navController.navigate(Routes.pastEdition(id)) } },
                    onOpenSettings = { runCatching { navController.navigate(Routes.SETTINGS) } },
                    onOpenTopics = { runCatching { navController.navigate(Routes.TOPICS) } },
                    onOpenDiagnostics = onOpenDiagnostics
                )
                onboardingGraph(
                    onFinished = {
                        runCatching { navController.navigate(Routes.MAIN) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        } }
                    }
                )
            }
        }
    }
}
