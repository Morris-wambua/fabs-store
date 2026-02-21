package com.morrislabs.fabs_store.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.morrislabs.fabs_store.data.api.ServicesApiService
import com.morrislabs.fabs_store.data.api.SetupApiService
import com.morrislabs.fabs_store.data.api.StoreApiService
import com.morrislabs.fabs_store.data.model.CreateStorePayload
import com.morrislabs.fabs_store.data.model.ExpertDTO
import com.morrislabs.fabs_store.data.model.FetchStoreResponse
import com.morrislabs.fabs_store.data.model.ReservationDTO
import com.morrislabs.fabs_store.data.model.TimeSlot
import com.morrislabs.fabs_store.data.model.ReservationWithPaymentDTO
import com.morrislabs.fabs_store.data.model.TypeOfServiceDTO
import com.morrislabs.fabs_store.data.model.UpdateStorePayload
import com.morrislabs.fabs_store.data.model.UserLookupResponseDTO
import com.morrislabs.fabs_store.data.repository.ExpertRepository
import com.morrislabs.fabs_store.data.repository.ReservationRepository
import com.morrislabs.fabs_store.data.repository.UserRepository
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
    private val servicesApiService = ServicesApiService(context, tokenManager)
    private val setupApiService = SetupApiService(context)
    private val expertRepository = ExpertRepository(context, tokenManager)
    private val userRepository = UserRepository(context, tokenManager)
    private val reservationRepository = ReservationRepository(context, tokenManager)

    private val _storeState = MutableStateFlow<StoreState>(StoreState.Idle)
    val storeState: StateFlow<StoreState> = _storeState.asStateFlow()

    private val _createStoreState = MutableStateFlow<CreateStoreState>(CreateStoreState.Idle)
    val createStoreState: StateFlow<CreateStoreState> = _createStoreState.asStateFlow()

    private val _updateStoreState = MutableStateFlow<UpdateStoreState>(UpdateStoreState.Idle)
    val updateStoreState: StateFlow<UpdateStoreState> = _updateStoreState.asStateFlow()

    private val _reservationsState = MutableStateFlow<LoadingState<List<ReservationWithPaymentDTO>>>(LoadingState.Idle)
    val reservationsState: StateFlow<LoadingState<List<ReservationWithPaymentDTO>>> = _reservationsState.asStateFlow()

    // Cache for reservations by filter status
    private val reservationsCache = mutableMapOf<String, List<ReservationWithPaymentDTO>>()
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Setup wizard states
    private val _servicesState = MutableStateFlow<LoadingState<List<TypeOfServiceDTO>>>(LoadingState.Idle)
    val servicesState: StateFlow<LoadingState<List<TypeOfServiceDTO>>> = _servicesState.asStateFlow()

    private val _categoriesState = MutableStateFlow<LoadingState<List<com.morrislabs.fabs_store.data.model.MainCategory>>>(LoadingState.Idle)
    val categoriesState: StateFlow<LoadingState<List<com.morrislabs.fabs_store.data.model.MainCategory>>> = _categoriesState.asStateFlow()

    private val _servicesByCategoryState = MutableStateFlow<LoadingState<List<TypeOfServiceDTO>>>(LoadingState.Idle)
    val servicesByCategoryState: StateFlow<LoadingState<List<TypeOfServiceDTO>>> = _servicesByCategoryState.asStateFlow()

    private val _walkInServicesState = MutableStateFlow<LoadingState<List<TypeOfServiceDTO>>>(LoadingState.Idle)
    val walkInServicesState: StateFlow<LoadingState<List<TypeOfServiceDTO>>> = _walkInServicesState.asStateFlow()

    private val _walkInExpertsState = MutableStateFlow<LoadingState<List<ExpertDTO>>>(LoadingState.Idle)
    val walkInExpertsState: StateFlow<LoadingState<List<ExpertDTO>>> = _walkInExpertsState.asStateFlow()

    private val _walkInAvailableSlotsByExpertState = MutableStateFlow<Map<String, LoadingState<List<TimeSlot>>>>(emptyMap())
    val walkInAvailableSlotsByExpertState: StateFlow<Map<String, LoadingState<List<TimeSlot>>>> =
        _walkInAvailableSlotsByExpertState.asStateFlow()

    private val _walkInBookingActionState = MutableStateFlow<WalkInBookingActionState>(WalkInBookingActionState.Idle)
    val walkInBookingActionState: StateFlow<WalkInBookingActionState> = _walkInBookingActionState.asStateFlow()

    private val _walkInCustomerLookupState = MutableStateFlow<WalkInCustomerLookupState>(WalkInCustomerLookupState.Idle)
    val walkInCustomerLookupState: StateFlow<WalkInCustomerLookupState> = _walkInCustomerLookupState.asStateFlow()

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
                    // Fetch reservations for this store
                    fetchReservations(store.id ?: "")
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

    fun fetchReservations(
        storeId: String,
        filterStatus: String = "ALL",
        query: String? = null,
        pageNumber: Int = 0,
        pageSize: Int = 20,
        forceRefresh: Boolean = false
    ) {
        val normalizedQuery = query?.trim()?.lowercase().orEmpty()
        val cacheKey = "$filterStatus|$normalizedQuery"
        // Check cache first if not forcing refresh
        if (!forceRefresh && reservationsCache.containsKey(cacheKey)) {
            Log.d(TAG, "Returning cached reservations for filter: $filterStatus (query: $normalizedQuery)")
            _reservationsState.value = LoadingState.Success(reservationsCache[cacheKey] ?: emptyList())
            return
        }

        _reservationsState.value = LoadingState.Loading

        viewModelScope.launch {
            Log.d(TAG, "Fetching reservations for store: $storeId (filter: $filterStatus, query: $normalizedQuery, page: $pageNumber, size: $pageSize, forceRefresh: $forceRefresh)")

            reservationRepository.fetchStoreReservations(storeId, filterStatus, normalizedQuery, pageNumber, pageSize)
                .onSuccess { reservations ->
                    Log.d(TAG, "Reservations fetched: ${reservations.size} items")
                    // Cache the results
                    reservationsCache[cacheKey] = reservations
                    _reservationsState.value = LoadingState.Success(reservations)
                    _isRefreshing.value = false
                }
                .onFailure { error ->
                    val errorMessage = error.message ?: "Failed to fetch reservations"
                    Log.e(TAG, "Fetch reservations failed: $errorMessage", error)
                    _reservationsState.value = LoadingState.Error(errorMessage)
                    _isRefreshing.value = false
                }
        }
    }

    fun refreshReservations(storeId: String, filterStatus: String = "ALL", query: String? = null) {
        // Prevent multiple refreshes in progress
        if (_isRefreshing.value) {
            Log.d(TAG, "Refresh already in progress, ignoring")
            return
        }

        _isRefreshing.value = true
        Log.d(TAG, "Refreshing reservations for filter: $filterStatus (query: ${query ?: ""})")
        fetchReservations(storeId, filterStatus, query, forceRefresh = true)
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

    fun fetchWalkInServices(storeId: String, query: String? = null) {
        _walkInServicesState.value = LoadingState.Loading

        viewModelScope.launch {
            servicesApiService.fetchServicesByStore(storeId = storeId, query = query)
                .onSuccess { services ->
                    _walkInServicesState.value = LoadingState.Success(services)
                }
                .onFailure { error ->
                    _walkInServicesState.value = LoadingState.Error(error.message ?: "Failed to fetch services")
                }
        }
    }

    fun fetchWalkInExperts(storeId: String) {
        _walkInExpertsState.value = LoadingState.Loading

        viewModelScope.launch {
            expertRepository.getExpertsByStoreId(storeId)
                .onSuccess { experts ->
                    _walkInExpertsState.value = LoadingState.Success(experts)
                }
                .onFailure { error ->
                    _walkInExpertsState.value = LoadingState.Error(error.message ?: "Failed to fetch experts")
                }
        }
    }

    fun lookupWalkInCustomerByEmail(email: String) {
        if (email.isBlank()) {
            _walkInCustomerLookupState.value = WalkInCustomerLookupState.Idle
            return
        }
        _walkInCustomerLookupState.value = WalkInCustomerLookupState.Loading
        viewModelScope.launch {
            userRepository.lookupUserByEmail(email)
                .onSuccess { user ->
                    _walkInCustomerLookupState.value = WalkInCustomerLookupState.Found(user)
                }
                .onFailure { error ->
                    val message = error.message ?: "Failed to lookup user"
                    if (message.contains("not found", ignoreCase = true)) {
                        _walkInCustomerLookupState.value = WalkInCustomerLookupState.NotFound
                    } else {
                        _walkInCustomerLookupState.value = WalkInCustomerLookupState.Error(message)
                    }
                }
        }
    }

    fun resetWalkInCustomerLookup() {
        _walkInCustomerLookupState.value = WalkInCustomerLookupState.Idle
    }

    fun fetchWalkInAvailableSlots(expertId: String, date: String, durationMinutes: Int) {
        _walkInAvailableSlotsByExpertState.value =
            _walkInAvailableSlotsByExpertState.value + (expertId to LoadingState.Loading)

        viewModelScope.launch {
            expertRepository.getAvailableTimeSlots(expertId, date, durationMinutes)
                .onSuccess { slots ->
                    _walkInAvailableSlotsByExpertState.value =
                        _walkInAvailableSlotsByExpertState.value + (expertId to LoadingState.Success(slots))
                }
                .onFailure { error ->
                    _walkInAvailableSlotsByExpertState.value =
                        _walkInAvailableSlotsByExpertState.value + (expertId to LoadingState.Error(error.message ?: "Failed to fetch time slots"))
                }
        }
    }

    fun clearWalkInAvailableSlots() {
        _walkInAvailableSlotsByExpertState.value = emptyMap()
    }

    fun createWalkInReservations(reservations: List<ReservationDTO>) {
        if (reservations.isEmpty()) {
            _walkInBookingActionState.value = WalkInBookingActionState.Error("No reservation payload to submit")
            return
        }
        _walkInBookingActionState.value = WalkInBookingActionState.Loading

        viewModelScope.launch {
            var createdCount = 0
            reservations.forEach { reservation ->
                val result = reservationRepository.createReservation(reservation)
                if (result.isFailure) {
                    _walkInBookingActionState.value = WalkInBookingActionState.Error(
                        result.exceptionOrNull()?.message ?: "Failed to create reservation"
                    )
                    return@launch
                }
                createdCount += 1
            }
            _walkInBookingActionState.value = WalkInBookingActionState.Success(createdCount)
            val storeId = (storeState.value as? StoreState.Success)?.data?.id.orEmpty()
            if (storeId.isNotBlank()) {
                fetchReservations(storeId, forceRefresh = true)
            }
        }
    }

    fun resetWalkInBookingActionState() {
        _walkInBookingActionState.value = WalkInBookingActionState.Idle
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

    fun updateStore(storeId: String, payload: UpdateStorePayload) {
        _updateStoreState.value = UpdateStoreState.Loading

        viewModelScope.launch {
            Log.d(TAG, "Updating store: $storeId")

            storeApiService.updateStore(storeId, payload)
                .onSuccess { updatedId ->
                    Log.d(TAG, "Store updated successfully: $updatedId")
                    _updateStoreState.value = UpdateStoreState.Success(updatedId)
                    // Refresh store data after successful update
                    fetchUserStore()
                }
                .onFailure { error ->
                    val errorMessage = error.message ?: "Unknown error"
                    Log.e(TAG, "Update store failed: $errorMessage", error)
                    _updateStoreState.value = UpdateStoreState.Error(errorMessage)
                }
        }
    }

    fun uploadStoreImage(
        uri: Uri,
        onSuccess: (url: String, key: String) -> Unit,
        onFailure: (message: String) -> Unit
    ) {
        viewModelScope.launch {
            val userId = tokenManager.getUserId()
            if (userId.isNullOrBlank()) {
                onFailure("User not authenticated")
                return@launch
            }

            storeApiService.uploadStoreImage(uri, userId)
                .onSuccess { (url, key) ->
                    onSuccess(url, key)
                }
                .onFailure { error ->
                    onFailure(error.message ?: "Failed to upload image")
                }
        }
    }

    fun resetStoreState() {
        _storeState.value = StoreState.Idle
    }

    fun resetCreateStoreState() {
        _createStoreState.value = CreateStoreState.Idle
    }

    fun resetUpdateStoreState() {
        _updateStoreState.value = UpdateStoreState.Idle
    }

    fun resetAllStates() {
        _storeState.value = StoreState.Idle
        _createStoreState.value = CreateStoreState.Idle
        _updateStoreState.value = UpdateStoreState.Idle
        _categoriesState.value = LoadingState.Idle
        _servicesByCategoryState.value = LoadingState.Idle
        _servicesState.value = LoadingState.Idle
        _walkInServicesState.value = LoadingState.Idle
        _walkInExpertsState.value = LoadingState.Idle
        _walkInAvailableSlotsByExpertState.value = emptyMap()
        _walkInBookingActionState.value = WalkInBookingActionState.Idle
        _walkInCustomerLookupState.value = WalkInCustomerLookupState.Idle
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

    sealed class UpdateStoreState {
        data object Idle : UpdateStoreState()
        data object Loading : UpdateStoreState()
        data class Success(val storeId: String) : UpdateStoreState()
        data class Error(val message: String) : UpdateStoreState()
    }

    sealed class WalkInBookingActionState {
        data object Idle : WalkInBookingActionState()
        data object Loading : WalkInBookingActionState()
        data class Success(val createdCount: Int) : WalkInBookingActionState()
        data class Error(val message: String) : WalkInBookingActionState()
    }

    sealed class WalkInCustomerLookupState {
        data object Idle : WalkInCustomerLookupState()
        data object Loading : WalkInCustomerLookupState()
        data class Found(val user: UserLookupResponseDTO) : WalkInCustomerLookupState()
        data object NotFound : WalkInCustomerLookupState()
        data class Error(val message: String) : WalkInCustomerLookupState()
    }
}
