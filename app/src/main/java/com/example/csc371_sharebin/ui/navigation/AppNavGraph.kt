package com.example.csc371_sharebin.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.csc371_sharebin.data.UserSession
import com.example.csc371_sharebin.ui.BinViewModel
import com.example.csc371_sharebin.ui.screens.AddBinScreen
import com.example.csc371_sharebin.ui.screens.BinListScreen
import com.example.csc371_sharebin.ui.screens.LandingScreen
import com.example.csc371_sharebin.ui.screens.MapScreen   // if you made this
import androidx.compose.ui.platform.LocalContext


/**
 * Main navigation graph.
 * Sets up all screens, routes, and how the app moves between them.
 * It also wires in things like login state, logout behavior, and ViewModel use.
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    viewModel: BinViewModel,
    startOnMap: Boolean = false,
    isUserLoggedIn: Boolean = false
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Landing.route
    ) {
        composable(Screen.Landing.route) {
            LandingScreen(
                onContinueToApp = {
                    viewModel.addSampleBinIfEmpty()
                    navController.navigate(Screen.Bins.route) {
                        popUpTo(Screen.Landing.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Bins.route) {
            val context = LocalContext.current
            BinListScreen(
                viewModel = viewModel,
                onAddBinClick = { navController.navigate(Screen.AddBin.route) },
                onMapClick = { navController.navigate(Screen.Map.route) },
                onLogoutClick = {
                    UserSession.clear(context)
                    navController.navigate(Screen.Landing.route) {
                        popUpTo(Screen.Bins.route) { inclusive = true }
                    }
                }
            )
        }


        composable(Screen.AddBin.route) {
            AddBinScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Map.route) {
            MapScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
    LaunchedEffect(startOnMap) {
                    if (startOnMap) {
                        navController.navigate("map") {
                            popUpTo("landing") { inclusive = true }
                        }
                    }
                }}
