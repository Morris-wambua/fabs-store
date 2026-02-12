package com.morrislabs.fabs_store

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.morrislabs.fabs_store.ui.screens.CreateStoreScreen
import com.morrislabs.fabs_store.ui.screens.EmployeesScreen
import com.morrislabs.fabs_store.ui.screens.HomeScreen
import com.morrislabs.fabs_store.ui.screens.LoginScreen
import com.morrislabs.fabs_store.ui.screens.RegisterScreen
import com.morrislabs.fabs_store.ui.screens.ReservationsScreen
import com.morrislabs.fabs_store.ui.screens.ServicesScreen
import com.morrislabs.fabs_store.ui.theme.FabsstoreTheme
import com.morrislabs.fabs_store.ui.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FabsstoreTheme {
                StoreApp()
            }
        }
    }
}

@Composable
fun StoreApp(
    authViewModel: AuthViewModel = viewModel()
) {
    val navController = rememberNavController()
    var isLoggedIn by remember { mutableStateOf(authViewModel.isLoggedIn()) }

    LaunchedEffect(Unit) {
        isLoggedIn = authViewModel.isLoggedIn()
    }

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) "home" else "login",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("login") {
            LoginScreen(
                onNavigateToRegister = { navController.navigate("register") },
                onLoginSuccess = { userId ->
                    isLoggedIn = true
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onForgotPassword = {
                    navController.navigate("forgot_password")
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onNavigateToLogin = { navController.navigate("login") { popUpTo("register") { inclusive = true } } },
                onRegisterSuccess = { userId ->
                    isLoggedIn = true
                    navController.navigate("home") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        composable("forgot_password") {
            // TODO: ForgotPasswordScreen will be implemented next
        }

        composable("home") {
            HomeScreen(
                onNavigateToReservations = { navController.navigate("reservations") },
                onNavigateToEmployees = { navController.navigate("employees") },
                onNavigateToServices = { navController.navigate("services") },
                onNavigateToCreateStore = { navController.navigate("create_store") },
                onLogout = {
                    authViewModel.logout()
                    isLoggedIn = false
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable("create_store") {
            CreateStoreScreen(
                onStoreCreated = {
                    navController.navigate("home") {
                        popUpTo("create_store") { inclusive = true }
                    }
                }
            )
        }

        composable("reservations") {
            ReservationsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable("employees") {
            EmployeesScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable("services") {
            ServicesScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}