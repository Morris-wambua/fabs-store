package com.morrislabs.fabs_store.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.morrislabs.fabs_store.data.api.ServicesApiService
import com.morrislabs.fabs_store.data.model.CreateServicePayload
import com.morrislabs.fabs_store.data.model.MainCategory
import com.morrislabs.fabs_store.data.model.SubCategory
import com.morrislabs.fabs_store.data.model.TypeOfServiceDTO
import com.morrislabs.fabs_store.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ServicesViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "ServicesViewModel"
    }

    private val context = application.applicationContext
    private val tokenManager = TokenManager.getInstance(context)
    private val servicesApiService = ServicesApiService(context, tokenManager)

    private val _servicesState = MutableStateFlow<ServicesState>(ServicesState.Idle)
    val servicesState: StateFlow<ServicesState> = _servicesState.asStateFlow()

    private val _saveServiceState = MutableStateFlow<SaveServiceState>(SaveServiceState.Idle)
    val saveServiceState: StateFlow<SaveServiceState> = _saveServiceState.asStateFlow()

    private val _deleteServiceState = MutableStateFlow<DeleteServiceState>(DeleteServiceState.Idle)
    val deleteServiceState: StateFlow<DeleteServiceState> = _deleteServiceState.asStateFlow()

    fun fetchServices(storeId: String) {
        _servicesState.value = ServicesState.Loading

        viewModelScope.launch {
            Log.d(TAG, "Fetching services for store: $storeId")

            servicesApiService.fetchServicesByStore(storeId)
                .onSuccess { services ->
                    Log.d(TAG, "Services fetched: ${services.size} items")
                    _servicesState.value = ServicesState.Success(services)
                }
                .onFailure { error ->
                    val errorMessage = error.message ?: "Failed to fetch services"
                    Log.e(TAG, "Fetch services failed: $errorMessage", error)
                    _servicesState.value = ServicesState.Error(errorMessage)
                }
        }
    }

    fun createService(storeId: String, payload: CreateServicePayload) {
        _saveServiceState.value = SaveServiceState.Loading

        viewModelScope.launch {
            Log.d(TAG, "Creating service for store: $storeId")

            servicesApiService.createService(storeId, payload)
                .onSuccess { serviceId ->
                    Log.d(TAG, "Service created: $serviceId")
                    _saveServiceState.value = SaveServiceState.Success(serviceId)
                    fetchServices(storeId)
                }
                .onFailure { error ->
                    val errorMessage = error.message ?: "Failed to create service"
                    Log.e(TAG, "Create service failed: $errorMessage", error)
                    _saveServiceState.value = SaveServiceState.Error(errorMessage)
                }
        }
    }

    fun updateService(storeId: String, serviceId: String, payload: CreateServicePayload) {
        _saveServiceState.value = SaveServiceState.Loading

        viewModelScope.launch {
            Log.d(TAG, "Updating service: $serviceId")

            servicesApiService.updateService(storeId, serviceId, payload)
                .onSuccess { updatedId ->
                    Log.d(TAG, "Service updated: $updatedId")
                    _saveServiceState.value = SaveServiceState.Success(updatedId)
                    fetchServices(storeId)
                }
                .onFailure { error ->
                    val errorMessage = error.message ?: "Failed to update service"
                    Log.e(TAG, "Update service failed: $errorMessage", error)
                    _saveServiceState.value = SaveServiceState.Error(errorMessage)
                }
        }
    }

    fun deleteService(storeId: String, serviceId: String) {
        _deleteServiceState.value = DeleteServiceState.Loading

        viewModelScope.launch {
            Log.d(TAG, "Deleting service: $serviceId")

            servicesApiService.deleteService(serviceId)
                .onSuccess {
                    Log.d(TAG, "Service deleted: $serviceId")
                    _deleteServiceState.value = DeleteServiceState.Success
                    fetchServices(storeId)
                }
                .onFailure { error ->
                    val errorMessage = error.message ?: "Failed to delete service"
                    Log.e(TAG, "Delete service failed: $errorMessage", error)
                    _deleteServiceState.value = DeleteServiceState.Error(errorMessage)
                }
        }
    }

    fun uploadServiceImage(
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

            servicesApiService.uploadServiceImage(uri, userId)
                .onSuccess { (url, key) -> onSuccess(url, key) }
                .onFailure { error -> onFailure(error.message ?: "Failed to upload image") }
        }
    }

    fun resetSaveState() {
        _saveServiceState.value = SaveServiceState.Idle
    }

    fun resetDeleteState() {
        _deleteServiceState.value = DeleteServiceState.Idle
    }

    fun getSubCategoriesForMain(mainCategory: MainCategory): List<SubCategory> {
        return SubCategory.entries.filter { it.toMainCategory() == mainCategory }
    }

    sealed class ServicesState {
        data object Idle : ServicesState()
        data object Loading : ServicesState()
        data class Success(val services: List<TypeOfServiceDTO>) : ServicesState()
        data class Error(val message: String) : ServicesState()
    }

    sealed class SaveServiceState {
        data object Idle : SaveServiceState()
        data object Loading : SaveServiceState()
        data class Success(val serviceId: String) : SaveServiceState()
        data class Error(val message: String) : SaveServiceState()
    }

    sealed class DeleteServiceState {
        data object Idle : DeleteServiceState()
        data object Loading : DeleteServiceState()
        data object Success : DeleteServiceState()
        data class Error(val message: String) : DeleteServiceState()
    }
}
