package com.example.csc371_sharebin.ui.navigation

/**
 * Defines all navigation routes used in the app.
 * Each object represents a screen the user can navigate to.
 */
sealed class Screen(val route: String) {

    data object Landing : Screen("landing")
    data object Bins : Screen("bins")
    data object AddBin : Screen("addBin")
    data object Map : Screen("map")
}
