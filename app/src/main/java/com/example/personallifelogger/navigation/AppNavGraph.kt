package com.example.personallifelogger.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.personallifelogger.ui.screens.AddEntryScreen
import com.example.personallifelogger.ui.screens.EntryDetailScreen
import com.example.personallifelogger.ui.screens.HomeScreen
import com.example.personallifelogger.viewmodel.AuthViewModel
import com.example.personallifelogger.viewmodel.EntryViewModel

object Routes {
    const val HOME = "home"
    const val ADD = "add"
    const val DETAIL = "detail/{entryId}"
    fun detail(id: Long) = "detail/$id"
}

@Composable
fun AppNavGraph(
    authViewModel: AuthViewModel,
    entryViewModel: EntryViewModel
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = entryViewModel,
                onAddClick = { navController.navigate(Routes.ADD) },
                onEntryClick = { id -> navController.navigate(Routes.detail(id)) },
                onSignOut = {
                    authViewModel.signOut()
                }
            )
        }
        composable(Routes.ADD) {
            AddEntryScreen(
                viewModel = entryViewModel,
                onSaved = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("entryId") { type = NavType.LongType })
        ) { backStack ->
            val id = backStack.arguments?.getLong("entryId") ?: 0L
            EntryDetailScreen(
                entryId = id,
                viewModel = entryViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}