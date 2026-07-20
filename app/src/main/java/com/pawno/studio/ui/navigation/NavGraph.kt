package com.pawno.studio.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pawno.studio.ui.screens.about.AboutScreen
import com.pawno.studio.ui.screens.amx.AmxInspectorScreen
import com.pawno.studio.ui.screens.diagnostics.DiagnosticsScreen
import com.pawno.studio.ui.screens.ide.MainIdeScreen
import com.pawno.studio.ui.screens.ide.MainIdeViewModel
import com.pawno.studio.ui.screens.libraries.LibraryManagerScreen
import com.pawno.studio.ui.screens.settings.SettingsScreen
import com.pawno.studio.ui.screens.splash.SplashScreen

@Composable
fun PawnoNavGraph(
    navController: NavHostController = rememberNavController(),
    ideViewModel: MainIdeViewModel,
    onOpenFolderPicker: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashComplete = {
                    navController.navigate(Screen.Ide.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Ide.route) {
            MainIdeScreen(
                viewModel = ideViewModel,
                onOpenFolderPicker = onOpenFolderPicker,
                onNavigateToDiagnostics = {
                    navController.navigate(Screen.Diagnostics.route)
                },
                onNavigateToAmxInspector = {
                    navController.navigate(Screen.AmxInspector.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToAbout = {
                    navController.navigate(Screen.About.route)
                },
                onNavigateToLibraries = {
                    navController.navigate(Screen.Libraries.route)
                }
            )
        }

        composable(Screen.Diagnostics.route) {
            val compileResult by ideViewModel.compileResult.collectAsState()
            DiagnosticsScreen(
                compileResult = compileResult,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onJumpToLine = { line ->
                    ideViewModel.jumpToLine(line)
                }
            )
        }

        composable(Screen.AmxInspector.route) {
            val amxHeader by ideViewModel.amxHeader.collectAsState()
            AmxInspectorScreen(
                amxHeader = amxHeader,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Libraries.route) {
            LibraryManagerScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
