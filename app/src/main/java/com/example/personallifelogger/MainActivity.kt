package com.example.personallifelogger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.personallifelogger.data.AuthState
import com.example.personallifelogger.navigation.AppNavGraph
import com.example.personallifelogger.ui.screens.LoginScreen
import com.example.personallifelogger.ui.theme.PersonalLifeLoggerTheme
import com.example.personallifelogger.viewmodel.AuthViewModel
import com.example.personallifelogger.viewmodel.EntryViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PersonalLifeLoggerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PersonalLifeLoggerApp()
                }
            }
        }
    }
}

@Composable
fun PersonalLifeLoggerApp() {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()
    val entryViewModel: EntryViewModel = viewModel()
    var isLoggedIn by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                val user = (authState as AuthState.Success).user
                if (user != null) {
                    // Set the userId in EntryViewModel
                    entryViewModel.setCurrentUserId(user.uid)
                    isLoggedIn = true
                } else {
                    isLoggedIn = false
                }
            }
            else -> {}
        }
    }

    if (isLoggedIn) {
        AppNavGraph(
            authViewModel = authViewModel,
            entryViewModel = entryViewModel
        )
    } else {
        LoginScreen(
            authViewModel = authViewModel,
            onLoginSuccess = {
                // Login success is handled by LaunchedEffect
            }
        )
    }
}