package com.morrislabs.fabs_store.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.morrislabs.fabs_store.data.model.ExpertDTO
import com.morrislabs.fabs_store.data.repository.ExpertRepository
import com.morrislabs.fabs_store.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExpertViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "ExpertViewModel"
    }

    private val context = application.applicationContext
    private val tokenManager = TokenManager.getInstance(context)
    private val repository = ExpertRepository(context, tokenManager)

    private val _expertsState = MutableStateFlow<ExpertsState>(ExpertsState.Idle)
    val expertsState: StateFlow<ExpertsState> = _expertsState.asStateFlow()

    private val _expertDetailsState = MutableStateFlow<ExpertDetailsState>(ExpertDetailsState.Idle)
    val expertDetailsState: StateFlow<ExpertDetailsState> = _expertDetailsState.asStateFlow()

    fun getExpertsByStoreId(storeId: String) {
        _expertsState.value = ExpertsState.Loading
        viewModelScope.launch {
            try {
                Log.d(TAG, "Fetching experts for store ID: $storeId")
                repository.getExpertsByStoreId(storeId)
                    .onSuccess { experts ->
                        Log.d(TAG, "Successfully fetched ${experts.size} experts for store")
                        _expertsState.value = ExpertsState.Success(experts)
                    }
                    .onFailure { error ->
                        Log.e(TAG, "Failed to fetch experts for store", error)
                        _expertsState.value = ExpertsState.Error(error.message ?: "Unknown error")
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while fetching experts for store", e)
                _expertsState.value = ExpertsState.Error("Failed to load experts: ${e.message}")
            }
        }
    }

    fun getExpertDetails(expertId: String) {
        _expertDetailsState.value = ExpertDetailsState.Loading
        viewModelScope.launch {
            try {
                Log.d(TAG, "Fetching details for expert ID: $expertId")
                repository.getExpertById(expertId)
                    .onSuccess { expert ->
                        Log.d(TAG, "Successfully fetched expert: ${expert.name}")
                        _expertDetailsState.value = ExpertDetailsState.Success(expert)
                    }
                    .onFailure { error ->
                        Log.e(TAG, "Failed to fetch expert details", error)
                        _expertDetailsState.value = ExpertDetailsState.Error(error.message ?: "Unknown error")
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while fetching expert details", e)
                _expertDetailsState.value = ExpertDetailsState.Error("Failed to load expert details: ${e.message}")
            }
        }
    }

    fun resetExpertsState() {
        _expertsState.value = ExpertsState.Idle
    }

    fun resetExpertDetailsState() {
        _expertDetailsState.value = ExpertDetailsState.Idle
    }

    sealed class ExpertsState {
        object Idle : ExpertsState()
        object Loading : ExpertsState()
        data class Success(val experts: List<ExpertDTO>) : ExpertsState()
        data class Error(val message: String) : ExpertsState()
    }

    sealed class ExpertDetailsState {
        object Idle : ExpertDetailsState()
        object Loading : ExpertDetailsState()
        data class Success(val expert: ExpertDTO) : ExpertDetailsState()
        data class Error(val message: String) : ExpertDetailsState()
    }
}
