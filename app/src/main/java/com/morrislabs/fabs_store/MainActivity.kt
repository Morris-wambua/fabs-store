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
import com.morrislabs.fabs_store.ui.screens.CreateStoreScreenRefactored
import com.morrislabs.fabs_store.ui.screens.EmployeesScreen
import com.morrislabs.fabs_store.ui.screens.CreateExpertScreen
import com.morrislabs.fabs_store.ui.screens.ExpertDetailsScreen
import com.morrislabs.fabs_store.ui.screens.HomeScreen
import com.morrislabs.fabs_store.ui.screens.LoginScreen
import com.morrislabs.fabs_store.ui.screens.RegisterScreen
import com.morrislabs.fabs_store.ui.screens.ReservationsScreen
import com.morrislabs.fabs_store.ui.screens.ServicesScreen
import com.morrislabs.fabs_store.ui.screens.SettingsScreen
import com.morrislabs.fabs_store.ui.screens.StoreProfileEditorScreen
import com.morrislabs.fabs_store.ui.theme.FabsstoreTheme
import com.morrislabs.fabs_store.ui.viewmodel.AuthViewModel
import com.morrislabs.fabs_store.util.AuthenticationStateListener
import com.morrislabs.fabs_store.util.ClientConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

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
    val storeViewModel: com.morrislabs.fabs_store.ui.viewmodel.StoreViewModel = viewModel()
    var isLoggedIn by remember { mutableStateOf(authViewModel.isLoggedIn()) }

    LaunchedEffect(Unit) {
        isLoggedIn = authViewModel.isLoggedIn()
        
        // Register auth state listener for session expiration
        ClientConfig.authStateListener = AuthenticationStateListener {
            // User session has expired - log them out
            // This callback may be invoked from background network thread, must switch to Main dispatcher
            runBlocking(Dispatchers.Main) {
                authViewModel.logout()
                isLoggedIn = false
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            }
        }
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
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToStoreProfile = { navController.navigate("store_profile_editor") },
                onNavigateToCreateStore = { navController.navigate("create_store") },
                onNavigateToExpertDetails = { expertId ->
                    navController.navigate("expert_details/$expertId")
                },
                onNavigateToCreateExpert = { storeId ->
                    navController.navigate("create_expert/$storeId")
                },
                onLogout = {
                    authViewModel.logout()
                    storeViewModel.resetAllStates()
                    isLoggedIn = false
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable("store_profile_editor") {
            StoreProfileEditorScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditLocation = {
                    // This would navigate to a dedicated location editor if needed
                    // For now, location editing is handled within the profile editor
                }
            )
        }

        composable("create_store") {
            CreateStoreScreenRefactored(
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
            EmployeesScreen(
                onNavigateBack = { navController.popBackStack() },
                onExpertSelected = { expertId ->
                    navController.navigate("expert_details/$expertId")
                },
                onNavigateToCreateExpert = { storeId ->
                    navController.navigate("create_expert/$storeId")
                }
            )
        }

        composable("expert_details/{expertId}") { backStackEntry ->
            val expertId = backStackEntry.arguments?.getString("expertId") ?: ""
            ExpertDetailsScreen(
                expertId = expertId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("create_expert/{storeId}") { backStackEntry ->
            val storeId = backStackEntry.arguments?.getString("storeId") ?: ""
            CreateExpertScreen(
                storeId = storeId,
                onNavigateBack = { navController.popBackStack() },
                onExpertCreated = {
                    navController.popBackStack()
                }
            )
        }

        composable("services") {
            ServicesScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    authViewModel.logout()
                    isLoggedIn = false
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}