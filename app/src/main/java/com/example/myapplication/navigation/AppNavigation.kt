package com.example.myapplication.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.screens.climate.ClimateScreen
import com.example.myapplication.screens.devices.DeviceListScreen
import com.example.myapplication.screens.journal.VerificationJournalScreen
import com.example.myapplication.screens.main.MainScreen
import com.example.myapplication.screens.scheduler.SchedulerScreen
import com.example.myapplication.screens.verification.VerificationScreen
import com.example.myapplication.viewmodels.verification.VerificationViewModel

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Climate : Screen("climate")
    object Scheduler : Screen("scheduler")
    object Verification : Screen("verification")
    object VerificationJournal : Screen("verification_journal")
    object DeviceList : Screen("device_list")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(
            route = Screen.Main.route,
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    )
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        ) { 
            MainScreen(navController) 
        }
        
        composable(
            route = Screen.Climate.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    )
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        ) {
            ClimateScreen(navController = navController)
        }
        
        composable(
            route = Screen.Scheduler.route,
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    )
                )
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        ) { 
            SchedulerScreen(navController) 
        }
        
        composable(
            route = Screen.Verification.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    )
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        ) {
            val verificationViewModel: VerificationViewModel = hiltViewModel()
            VerificationScreen(
                navController = navController,
                viewModel = verificationViewModel
            )
        }
        
        composable(
            route = Screen.VerificationJournal.route,
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    )
                )
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        ) { 
            VerificationJournalScreen(navController) 
        }
        
        composable(
            route = Screen.DeviceList.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    )
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        ) {
            val verificationViewModel: VerificationViewModel = hiltViewModel()
            DeviceListScreen(
                viewModel = verificationViewModel,
                onBack = { navController.navigateBack() }
            )
        }
    }
}