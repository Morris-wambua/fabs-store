package com.morrislabs.fabs_store.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.morrislabs.fabs_store.data.model.CreateExpertPayload
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

    private val _createExpertState = MutableStateFlow<CreateExpertState>(CreateExpertState.Idle)
    val createExpertState: StateFlow<CreateExpertState> = _createExpertState.asStateFlow()

    private val _updateExpertState = MutableStateFlow<UpdateExpertState>(UpdateExpertState.Idle)
    val updateExpertState: StateFlow<UpdateExpertState> = _updateExpertState.asStateFlow()

    private val _deleteExpertState = MutableStateFlow<DeleteExpertState>(DeleteExpertState.Idle)
    val deleteExpertState: StateFlow<DeleteExpertState> = _deleteExpertState.asStateFlow()

    private val _expertLeavesState = MutableStateFlow<ExpertLeavesState>(ExpertLeavesState.Idle)
    val expertLeavesState: StateFlow<ExpertLeavesState> = _expertLeavesState.asStateFlow()

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

    fun createExpert(storeId: String, payload: CreateExpertPayload) {
        _createExpertState.value = CreateExpertState.Loading
        viewModelScope.launch {
            try {
                Log.d(TAG, "Creating expert for store: $storeId")
                repository.createExpertForStore(storeId, payload)
                    .onSuccess { expertId ->
                        Log.d(TAG, "Successfully created expert: $expertId")
                        _createExpertState.value = CreateExpertState.Success(expertId)
                        getExpertsByStoreId(storeId)
                    }
                    .onFailure { error ->
                        Log.e(TAG, "Failed to create expert", error)
                        _createExpertState.value = CreateExpertState.Error(error.message ?: "Unknown error")
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while creating expert", e)
                _createExpertState.value = CreateExpertState.Error("Failed to create expert: ${e.message}")
            }
        }
    }

    fun resetCreateExpertState() {
        _createExpertState.value = CreateExpertState.Idle
    }

    fun updateExpert(expertId: String, storeId: String, payload: CreateExpertPayload) {
        _updateExpertState.value = UpdateExpertState.Loading
        viewModelScope.launch {
            repository.updateExpert(expertId, payload)
                .onSuccess {
                    _updateExpertState.value = UpdateExpertState.Success(expertId)
                    getExpertDetails(expertId)
                    getExpertsByStoreId(storeId)
                }
                .onFailure { error ->
                    _updateExpertState.value = UpdateExpertState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun deleteExpert(expertId: String, storeId: String) {
        _deleteExpertState.value = DeleteExpertState.Loading
        viewModelScope.launch {
            repository.deleteExpert(expertId)
                .onSuccess {
                    _deleteExpertState.value = DeleteExpertState.Success
                    getExpertsByStoreId(storeId)
                }
                .onFailure { error ->
                    _deleteExpertState.value = DeleteExpertState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun getExpertLeaves(expertId: String) {
        _expertLeavesState.value = ExpertLeavesState.Loading
        viewModelScope.launch {
            repository.getExpertLeaves(expertId)
                .onSuccess { leaves ->
                    _expertLeavesState.value = ExpertLeavesState.Success(leaves)
                }
                .onFailure { error ->
                    _expertLeavesState.value = ExpertLeavesState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun setExpertLeaveRange(expertId: String, startDate: String, endDate: String, reason: String? = null) {
        viewModelScope.launch {
            repository.setExpertLeaveRange(expertId, startDate, endDate, reason)
                .onSuccess { getExpertLeaves(expertId) }
        }
    }

    fun deleteExpertLeaveRange(expertId: String, startDate: String, endDate: String) {
        viewModelScope.launch {
            repository.deleteExpertLeaveRange(expertId, startDate, endDate)
                .onSuccess { getExpertLeaves(expertId) }
        }
    }

    suspend fun uploadExpertPhoto(uri: Uri): Result<Pair<String, String>> {
        val userId = TokenManager.getInstance(context).getUserId() ?: ""
        return repository.uploadExpertPhoto(uri, userId)
    }

    fun resetUpdateExpertState() { _updateExpertState.value = UpdateExpertState.Idle }
    fun resetDeleteExpertState() { _deleteExpertState.value = DeleteExpertState.Idle }

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

    sealed class CreateExpertState {
        object Idle : CreateExpertState()
        object Loading : CreateExpertState()
        data class Success(val expertId: String) : CreateExpertState()
        data class Error(val message: String) : CreateExpertState()
    }

    sealed class UpdateExpertState {
        object Idle : UpdateExpertState()
        object Loading : UpdateExpertState()
        data class Success(val expertId: String) : UpdateExpertState()
        data class Error(val message: String) : UpdateExpertState()
    }

    sealed class DeleteExpertState {
        object Idle : DeleteExpertState()
        object Loading : DeleteExpertState()
        object Success : DeleteExpertState()
        data class Error(val message: String) : DeleteExpertState()
    }

    sealed class ExpertLeavesState {
        object Idle : ExpertLeavesState()
        object Loading : ExpertLeavesState()
        data class Success(val leaves: List<com.morrislabs.fabs_store.data.model.ExpertLeaveDTO>) : ExpertLeavesState()
        data class Error(val message: String) : ExpertLeavesState()
    }
}
