package com.pawno.studio.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Ide : Screen("ide")
    object Diagnostics : Screen("diagnostics")
    object AmxInspector : Screen("amx_inspector")
    object About : Screen("about")
    object Settings : Screen("settings")
    object Libraries : Screen("libraries")
}
