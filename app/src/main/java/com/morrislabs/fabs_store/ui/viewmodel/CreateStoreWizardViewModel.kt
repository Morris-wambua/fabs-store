package com.morrislabs.fabs_store.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.morrislabs.fabs_store.data.api.StoreApiService
import com.morrislabs.fabs_store.data.model.BusinessHourDTO
import com.morrislabs.fabs_store.data.model.CreateStorePayload
import com.morrislabs.fabs_store.data.model.LocationDTO
import com.morrislabs.fabs_store.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class DaySchedule(
    val dayName: String,
    val dayIndex: Int,
    val isOpen: Boolean = true,
    val openTime: String = "09:00 AM",
    val closeTime: String = "05:00 PM"
)

data class StoreRegistrationState(
    val storeName: String = "",
    val storeHandle: String = "",
    val contactNumber: String = "",
    val aboutStore: String = "",
    val storeLogoUri: Uri? = null,
    val countryCode: String = "+254",
    val locationName: String = "",
    val locationDescription: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val locationSelected: Boolean = false,
    val businessHours: List<DaySchedule> = defaultBusinessHours(),
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val submitError: String? = null
)

fun defaultBusinessHours(): List<DaySchedule> = listOf(
    DaySchedule(dayName = "Monday", dayIndex = 0, isOpen = true),
    DaySchedule(dayName = "Tuesday", dayIndex = 1, isOpen = true),
    DaySchedule(dayName = "Wednesday", dayIndex = 2, isOpen = true),
    DaySchedule(dayName = "Thursday", dayIndex = 3, isOpen = true),
    DaySchedule(dayName = "Friday", dayIndex = 4, isOpen = true),
    DaySchedule(dayName = "Saturday", dayIndex = 5, isOpen = false),
    DaySchedule(dayName = "Sunday", dayIndex = 6, isOpen = false)
)

class CreateStoreWizardViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "CreateStoreWizardVM"
    }

    private val context = application.applicationContext
    private val tokenManager = TokenManager.getInstance(context)
    private val storeApiService = StoreApiService(context, tokenManager)

    private val _state = MutableStateFlow(StoreRegistrationState())
    val state: StateFlow<StoreRegistrationState> = _state.asStateFlow()

    fun updateStoreName(name: String) {
        _state.value = _state.value.copy(storeName = name)
    }

    fun updateStoreHandle(handle: String) {
        _state.value = _state.value.copy(storeHandle = handle)
    }

    fun updateContactNumber(number: String) {
        _state.value = _state.value.copy(contactNumber = number)
    }

    fun updateAboutStore(about: String) {
        _state.value = _state.value.copy(aboutStore = about)
    }

    fun updateStoreLogoUri(uri: Uri?) {
        _state.value = _state.value.copy(storeLogoUri = uri)
    }

    fun updateCountryCode(code: String) {
        _state.value = _state.value.copy(countryCode = code)
    }

    fun updateLocation(name: String, description: String, lat: Double, lng: Double) {
        _state.value = _state.value.copy(
            locationName = name,
            locationDescription = description,
            latitude = lat,
            longitude = lng,
            locationSelected = true
        )
    }

    fun toggleDayOpen(dayIndex: Int) {
        _state.value = _state.value.copy(
            businessHours = _state.value.businessHours.map { day ->
                if (day.dayIndex == dayIndex) day.copy(isOpen = !day.isOpen) else day
            }
        )
    }

    fun updateOpenTime(dayIndex: Int, time: String) {
        _state.value = _state.value.copy(
            businessHours = _state.value.businessHours.map { day ->
                if (day.dayIndex == dayIndex) day.copy(openTime = time) else day
            }
        )
    }

    fun updateCloseTime(dayIndex: Int, time: String) {
        _state.value = _state.value.copy(
            businessHours = _state.value.businessHours.map { day ->
                if (day.dayIndex == dayIndex) day.copy(closeTime = time) else day
            }
        )
    }

    fun copyMondayToAllDays() {
        val monday = _state.value.businessHours.firstOrNull { it.dayIndex == 0 } ?: return
        _state.value = _state.value.copy(
            businessHours = _state.value.businessHours.map { day ->
                day.copy(isOpen = monday.isOpen, openTime = monday.openTime, closeTime = monday.closeTime)
            }
        )
    }

    fun canProceedFromStep1(): Boolean {
        val current = _state.value
        return current.storeName.isNotBlank() && current.storeHandle.isNotBlank()
    }

    fun canProceedFromStep2(): Boolean {
        return _state.value.locationSelected
    }

    fun canSubmit(): Boolean {
        return canProceedFromStep1() && canProceedFromStep2()
    }

    fun submitStore() {
        if (!canSubmit()) return

        _state.value = _state.value.copy(isSubmitting = true, submitError = null)

        viewModelScope.launch {
            val current = _state.value

            var logoUrl: String? = null
            var logoS3Key: String? = null
            if (current.storeLogoUri != null) {
                val userId = TokenManager.getInstance(context).getUserId() ?: ""
                storeApiService.uploadStoreLogo(current.storeLogoUri, userId)
                    .onSuccess { (url, key) ->
                        logoUrl = url
                        logoS3Key = key
                    }
                    .onFailure { error ->
                        Log.w(TAG, "Logo upload failed, continuing without logo: ${error.message}")
                    }
            }

            val location = LocationDTO(
                id = UUID.randomUUID().toString(),
                name = current.locationName,
                description = current.locationDescription,
                latitude = current.latitude,
                longitude = current.longitude
            )

            val businessHourDTOs = current.businessHours.map { day ->
                BusinessHourDTO(
                    dayName = day.dayName,
                    dayIndex = day.dayIndex,
                    isOpen = day.isOpen,
                    openTime = if (day.isOpen) day.openTime else null,
                    closeTime = if (day.isOpen) day.closeTime else null
                )
            }

            val fullPhone = if (current.contactNumber.isNotBlank()) {
                "${current.countryCode}${current.contactNumber}"
            } else null

            val payload = CreateStorePayload(
                name = current.storeName,
                username = current.storeHandle,
                location = location,
                phone = fullPhone,
                about = current.aboutStore.ifBlank { null },
                logoUrl = logoUrl,
                logoS3Key = logoS3Key,
                businessHours = businessHourDTOs
            )

            Log.d(TAG, "Submitting store: ${payload.name}")

            storeApiService.createStore(payload)
                .onSuccess { storeId ->
                    Log.d(TAG, "Store created successfully with ID: $storeId")
                    _state.value = _state.value.copy(
                        isSubmitting = false,
                        submitSuccess = true
                    )
                }
                .onFailure { error ->
                    val errorMessage = error.message ?: "An unexpected error occurred"
                    Log.e(TAG, "Store creation failed: $errorMessage", error)
                    _state.value = _state.value.copy(
                        isSubmitting = false,
                        submitError = errorMessage
                    )
                }
        }
    }

    fun resetState() {
        _state.value = StoreRegistrationState()
    }
}
