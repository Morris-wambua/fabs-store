package com.morrislabs.fabs_store.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.morrislabs.fabs_store.data.api.SetupApiService
import com.morrislabs.fabs_store.data.api.StoreApiService
import com.morrislabs.fabs_store.data.model.CreateStorePayload
import com.morrislabs.fabs_store.data.model.FetchStoreResponse
import com.morrislabs.fabs_store.data.model.LocationDTO
import com.morrislabs.fabs_store.data.model.TypeOfServiceDTO
import com.morrislabs.fabs_store.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StoreViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "StoreViewModel"
    }

    private val context = application.applicationContext
    private val tokenManager = TokenManager.getInstance(context)
    private val storeApiService = StoreApiService(context, tokenManager)
    private val setupApiService = SetupApiService(context)

    private val _storeState = MutableStateFlow<StoreState>(StoreState.Idle)
    val storeState: StateFlow<StoreState> = _storeState.asStateFlow()

    private val _createStoreState = MutableStateFlow<CreateStoreState>(CreateStoreState.Idle)
    val createStoreState: StateFlow<CreateStoreState> = _createStoreState.asStateFlow()

    // Setup wizard states
    private val _servicesState = MutableStateFlow<LoadingState<List<TypeOfServiceDTO>>>(LoadingState.Idle)
    val servicesState: StateFlow<LoadingState<List<TypeOfServiceDTO>>> = _servicesState.asStateFlow()

    private val _categoriesState = MutableStateFlow<LoadingState<List<com.morrislabs.fabs_store.data.model.MainCategory>>>(LoadingState.Idle)
    val categoriesState: StateFlow<LoadingState<List<com.morrislabs.fabs_store.data.model.MainCategory>>> = _categoriesState.asStateFlow()

    private val _servicesByCategoryState = MutableStateFlow<LoadingState<List<TypeOfServiceDTO>>>(LoadingState.Idle)
    val servicesByCategoryState: StateFlow<LoadingState<List<TypeOfServiceDTO>>> = _servicesByCategoryState.asStateFlow()

    fun fetchUserStore() {
        _storeState.value = StoreState.Loading

        viewModelScope.launch {
            val userId = tokenManager.getUserId()
            
            if (userId == null) {
                _storeState.value = StoreState.Error.Unauthorized("User not authenticated")
                return@launch
            }

            Log.d(TAG, "Fetching store for user: $userId")

            storeApiService.fetchUserStore(userId)
                .onSuccess { store ->
                    Log.d(TAG, "Store fetched successfully: ${store.name}")
                    _storeState.value = StoreState.Success(store)
                }
                .onFailure { error ->
                    val errorMessage = error.message ?: "Unknown error"
                    Log.e(TAG, "Fetch store failed: $errorMessage", error)

                    _storeState.value = when {
                        errorMessage.contains("No store found", ignoreCase = true) -> {
                            StoreState.Error.NotFound(errorMessage)
                        }
                        errorMessage.contains("Unauthorized", ignoreCase = true) -> {
                            StoreState.Error.Unauthorized(errorMessage)
                        }
                        else -> {
                            StoreState.Error.UnknownError(errorMessage)
                        }
                    }
                }
        }
    }

    fun fetchServices() {
        _servicesState.value = LoadingState.Loading

        viewModelScope.launch {
            Log.d(TAG, "Fetching available services")

            setupApiService.fetchAllServices()
                .onSuccess { services ->
                    Log.d(TAG, "Services fetched: ${services.size} items")
                    _servicesState.value = LoadingState.Success(services)
                }
                .onFailure { error ->
                    val errorMessage = error.message ?: "Failed to fetch services"
                    Log.e(TAG, "Fetch services failed: $errorMessage", error)
                    _servicesState.value = LoadingState.Error(errorMessage)
                }
        }
    }

    fun fetchCategories() {
        _categoriesState.value = LoadingState.Loading

        viewModelScope.launch {
            Log.d(TAG, "Fetching service categories")

            setupApiService.fetchMainCategories()
                .onSuccess { categories ->
                    Log.d(TAG, "Categories fetched: ${categories.size} items")
                    _categoriesState.value = LoadingState.Success(categories)
                }
                .onFailure { error ->
                    val errorMessage = error.message ?: "Failed to fetch categories"
                    Log.e(TAG, "Fetch categories failed: $errorMessage", error)
                    _categoriesState.value = LoadingState.Error(errorMessage)
                }
        }
    }

    fun fetchServicesByCategory(category: com.morrislabs.fabs_store.data.model.MainCategory) {
        _servicesByCategoryState.value = LoadingState.Loading

        viewModelScope.launch {
            Log.d(TAG, "Fetching services for category: $category")

            setupApiService.fetchServicesByCategory(category)
                .onSuccess { services ->
                    Log.d(TAG, "Services fetched for category $category: ${services.size} items")
                    _servicesByCategoryState.value = LoadingState.Success(services)
                }
                .onFailure { error ->
                    val errorMessage = error.message ?: "Failed to fetch services"
                    Log.e(TAG, "Fetch services by category failed: $errorMessage", error)
                    _servicesByCategoryState.value = LoadingState.Error(errorMessage)
                }
        }
    }

    fun createStore(payload: CreateStorePayload) {
        _createStoreState.value = CreateStoreState.Loading

        viewModelScope.launch {
            Log.d(TAG, "Creating store: ${payload.name}")

            storeApiService.createStore(payload)
                .onSuccess { storeId ->
                    Log.d(TAG, "Store created successfully with ID: $storeId")
                    _createStoreState.value = CreateStoreState.Success(storeId)
                    fetchUserStore()
                }
                .onFailure { error ->
                    val errorMessage = error.message ?: "Unknown error"
                    Log.e(TAG, "Create store failed: $errorMessage", error)
                    _createStoreState.value = CreateStoreState.Error(errorMessage)
                }
        }
    }

    fun resetStoreState() {
        _storeState.value = StoreState.Idle
    }

    fun resetCreateStoreState() {
        _createStoreState.value = CreateStoreState.Idle
    }

    sealed class StoreState {
        data object Idle : StoreState()
        data object Loading : StoreState()
        data class Success(val data: FetchStoreResponse) : StoreState()

        sealed class Error(open val message: String) : StoreState() {
            data class NotFound(override val message: String) : Error(message)
            data class Unauthorized(override val message: String) : Error(message)
            data class UnknownError(override val message: String) : Error(message)
        }
    }

    sealed class CreateStoreState {
        data object Idle : CreateStoreState()
        data object Loading : CreateStoreState()
        data class Success(val storeId: String) : CreateStoreState()
        data class Error(val message: String) : CreateStoreState()
    }

    sealed class LoadingState<out T> {
        data object Idle : LoadingState<Nothing>()
        data object Loading : LoadingState<Nothing>()
        data class Success<T>(val data: T) : LoadingState<T>()
        data class Error<T>(val message: String) : LoadingState<T>()
    }
}
