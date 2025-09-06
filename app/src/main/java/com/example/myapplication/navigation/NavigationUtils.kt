package com.example.myapplication.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination

/**
 * Navigate to a screen with optional animation and clearing of back stack
 */
fun NavController.navigateTo(
    screen: Screen,
    clearBackStack: Boolean = false
) {
    if (clearBackStack) {
        // Pop up to the start destination of the graph
        popBackStack(graph.findStartDestination().id, inclusive = true)
    }
    navigate(screen.route)
}

/**
 * Navigate back to the previous screen
 */
fun NavController.navigateBack() {
    popBackStack()
} 