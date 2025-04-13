package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.viewmodels.ClimateViewModel
import com.example.myapplication.data.dao.ClimateDao
import com.example.myapplication.screens.ClimateScreen
import com.example.myapplication.screens.MainScreen
import com.example.myapplication.screens.SchedulerScreen
import com.example.myapplication.screens.VerificationJournalScreen
import com.example.myapplication.screens.VerificationScreen

/**
 * Основной компонент навигации приложения
 */
@Composable
fun AppNavigation(climateDao: ClimateDao) {
    val navController = rememberNavController()
    val viewModel = remember { ClimateViewModel(climateDao) }

    NavHost(navController, startDestination = "main") {
        composable("main") { MainScreen(navController) }
        composable("climate") { ClimateScreen(navController, viewModel) }
        composable("scheduler") { SchedulerScreen(navController) }
        composable("verification") { VerificationScreen(navController) }
        composable("VerificationJournal") { VerificationJournalScreen(navController) }
    }
}