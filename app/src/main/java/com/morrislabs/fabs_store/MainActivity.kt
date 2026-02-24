package com.morrislabs.fabs_store

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.morrislabs.fabs_store.ui.screens.EmployeesScreen
import com.morrislabs.fabs_store.ui.screens.CreateExpertScreen
import com.morrislabs.fabs_store.ui.screens.EditExpertProfileScreen
import com.morrislabs.fabs_store.ui.screens.ExpertDetailsScreen
import com.morrislabs.fabs_store.ui.screens.HomeScreen
import com.morrislabs.fabs_store.ui.screens.LoginScreen
import com.morrislabs.fabs_store.ui.screens.RegisterScreen
import com.morrislabs.fabs_store.ui.screens.ResetPasswordScreen
import com.morrislabs.fabs_store.ui.screens.ReservationsScreen
import com.morrislabs.fabs_store.ui.screens.services.AddServiceScreen
import com.morrislabs.fabs_store.ui.screens.services.ServiceDetailsScreen
import com.morrislabs.fabs_store.ui.screens.services.ServicesManagementListScreen
import com.morrislabs.fabs_store.ui.screens.DailyScheduleScreen
import com.morrislabs.fabs_store.ui.screens.SettingsScreen
import com.morrislabs.fabs_store.ui.screens.StoreProfileBusinessHoursScreen
import com.morrislabs.fabs_store.ui.screens.StoreProfileEditorScreen
import com.morrislabs.fabs_store.ui.screens.StoreProfileLocationEditorScreen
import com.morrislabs.fabs_store.ui.screens.SetupChecklistScreen
import com.morrislabs.fabs_store.ui.screens.posts.CreatePostScreen
import com.morrislabs.fabs_store.ui.screens.posts.LiveStreamScreen
import com.morrislabs.fabs_store.ui.screens.posts.LiveStreamSummaryScreen
import com.morrislabs.fabs_store.ui.screens.posts.PostDetailScreen
import com.morrislabs.fabs_store.ui.screens.storeonboarding.BusinessHoursStepScreen
import com.morrislabs.fabs_store.ui.screens.storeonboarding.StoreInfoStepScreen
import com.morrislabs.fabs_store.ui.screens.storeonboarding.StoreLocationStepScreen
import com.morrislabs.fabs_store.ui.theme.FabsstoreTheme
import com.morrislabs.fabs_store.ui.viewmodel.AuthViewModel
import com.morrislabs.fabs_store.ui.viewmodel.CreateStoreWizardViewModel
import com.morrislabs.fabs_store.ui.viewmodel.PostViewModel
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
    val servicesViewModel: com.morrislabs.fabs_store.ui.viewmodel.ServicesViewModel = viewModel()
    val postViewModel: PostViewModel = viewModel()
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
            ResetPasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
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
                onNavigateToDailySchedule = { navController.navigate("daily_schedule") },
                onNavigateToCreatePost = { navController.navigate("create_post") },
                onNavigateToPostDetail = { postId ->
                    navController.navigate("post_detail/$postId")
                },
                onLogout = {
                    authViewModel.logout()
                    storeViewModel.resetAllStates()
                    isLoggedIn = false
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                storeViewModel = storeViewModel,
                postViewModel = postViewModel
            )
        }

        composable("store_profile_editor") {
            StoreProfileEditorScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditLocation = {
                    navController.navigate("store_profile_editor/location")
                },
                onNavigateToBusinessHours = {
                    navController.navigate("store_profile_editor/hours")
                }
            )
        }

        composable("store_profile_editor/location") {
            StoreProfileLocationEditorScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("store_profile_editor/hours") {
            StoreProfileBusinessHoursScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        navigation(
            startDestination = "create_store/info",
            route = "create_store"
        ) {
            composable("create_store/info") { backStackEntry ->
                val parentEntry = remember(navController) {
                    navController.getBackStackEntry("create_store")
                }
                val wizardViewModel: CreateStoreWizardViewModel = viewModel(parentEntry)
                StoreInfoStepScreen(
                    wizardViewModel = wizardViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateNext = { navController.navigate("create_store/location") }
                )
            }

            composable("create_store/location") { backStackEntry ->
                val parentEntry = remember(navController) {
                    navController.getBackStackEntry("create_store")
                }
                val wizardViewModel: CreateStoreWizardViewModel = viewModel(parentEntry)
                StoreLocationStepScreen(
                    wizardViewModel = wizardViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateNext = { navController.navigate("create_store/hours") }
                )
            }

            composable("create_store/hours") { backStackEntry ->
                val parentEntry = remember(navController) {
                    navController.getBackStackEntry("create_store")
                }
                val wizardViewModel: CreateStoreWizardViewModel = viewModel(parentEntry)
                BusinessHoursStepScreen(
                    wizardViewModel = wizardViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onStoreCreated = {
                        navController.navigate("setup_checklist") {
                            popUpTo("create_store") { inclusive = true }
                        }
                    }
                )
            }
        }

        composable("setup_checklist") {
            SetupChecklistScreen(
                onNavigateBack = {
                    navController.navigate("home") {
                        popUpTo("setup_checklist") { inclusive = true }
                    }
                },
                onNavigateToServices = { navController.navigate("services") },
                onNavigateToCreateExpert = { storeId ->
                    navController.navigate("create_expert/$storeId")
                },
                onNavigateToCreatePost = {
                    navController.navigate("create_post")
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
                },
                storeViewModel = storeViewModel
            )
        }

        composable("expert_details/{expertId}") { backStackEntry ->
            val expertId = backStackEntry.arguments?.getString("expertId") ?: ""
            ExpertDetailsScreen(
                expertId = expertId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditExpert = { id, storeId ->
                    navController.navigate("edit_expert/$id/$storeId")
                }
            )
        }

        composable("edit_expert/{expertId}/{storeId}") { backStackEntry ->
            val expertId = backStackEntry.arguments?.getString("expertId") ?: ""
            val storeId = backStackEntry.arguments?.getString("storeId") ?: ""
            EditExpertProfileScreen(
                expertId = expertId,
                storeId = storeId,
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
            ServicesManagementListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddService = { navController.navigate("add_service") },
                onNavigateToServiceDetails = { serviceId ->
                    navController.navigate("service_details/$serviceId")
                },
                storeViewModel = storeViewModel,
                servicesViewModel = servicesViewModel
            )
        }

        composable("add_service") {
            AddServiceScreen(
                onNavigateBack = { navController.popBackStack() },
                onServiceSaved = { navController.popBackStack() },
                storeViewModel = storeViewModel,
                servicesViewModel = servicesViewModel
            )
        }

        composable("service_details/{serviceId}") { backStackEntry ->
            val serviceId = backStackEntry.arguments?.getString("serviceId") ?: ""
            val servicesState by servicesViewModel.servicesState.collectAsState()
            val existingService = (servicesState as? com.morrislabs.fabs_store.ui.viewmodel.ServicesViewModel.ServicesState.Success)
                ?.services?.find { it.id == serviceId }
            if (existingService != null) {
                ServiceDetailsScreen(
                    service = existingService,
                    onNavigateBack = { navController.popBackStack() },
                    onServiceSaved = { navController.popBackStack() },
                    storeViewModel = storeViewModel,
                    servicesViewModel = servicesViewModel
                )
            }
        }

        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToStoreProfile = { navController.navigate("store_profile_editor") },
                onLogout = {
                    authViewModel.logout()
                    isLoggedIn = false
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable("daily_schedule") {
            DailyScheduleScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("create_post") {
            val storeState by storeViewModel.storeState.collectAsState()
            val storeId = (storeState as? com.morrislabs.fabs_store.ui.viewmodel.StoreViewModel.StoreState.Success)?.data?.id.orEmpty()
            CreatePostScreen(
                storeId = storeId,
                postViewModel = postViewModel,
                onNavigateBack = { navController.popBackStack() },
                onPostCreated = { navController.popBackStack() },
                onNavigateToLiveStream = { navController.navigate("live_stream") }
            )
        }

        composable("post_detail/{postId}") { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            PostDetailScreen(
                postId = postId,
                postViewModel = postViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("live_stream") {
            LiveStreamScreen(
                onEndLive = {
                    navController.navigate("live_stream_summary") {
                        popUpTo("live_stream") { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("live_stream_summary") {
            LiveStreamSummaryScreen(
                onNavigateToPost = { navController.popBackStack() },
                onDone = {
                    navController.navigate("home") {
                        popUpTo("live_stream_summary") { inclusive = true }
                    }
                }
            )
        }
    }
}
