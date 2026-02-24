package com.morrislabs.fabs_store.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.morrislabs.fabs_store.data.model.LoginDTO
import com.morrislabs.fabs_store.data.repository.AuthRepository
import com.morrislabs.fabs_store.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "AuthViewModel"
    }

    private val repository = AuthRepository()
    private val tokenManager = TokenManager.getInstance(application.applicationContext)

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()

    private val _googleAuthState = MutableStateFlow<GoogleAuthState>(GoogleAuthState.Idle)
    val googleAuthState: StateFlow<GoogleAuthState> = _googleAuthState.asStateFlow()

    private val _resetPasswordState = MutableStateFlow<ResetPasswordState>(ResetPasswordState.Idle)
    val resetPasswordState: StateFlow<ResetPasswordState> = _resetPasswordState.asStateFlow()

    fun login(email: String, password: String) {
        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            Log.d(TAG, "Attempting login for: $email")

            repository.login(login = email, password = password)
                .onSuccess { loginDTO ->
                    Log.d(TAG, "Login successful")

                    loginDTO.token?.let { tokenManager.saveToken(it) }
                    loginDTO.refreshToken?.let { tokenManager.saveRefreshToken(it) }

                    if (loginDTO.id != null && loginDTO.firstName != null && loginDTO.lastName != null) {
                        tokenManager.saveUserInfo(loginDTO.id, loginDTO.firstName, loginDTO.lastName)
                    }

                    _loginState.value = LoginState.Success(loginDTO)
                }
                .onFailure { error ->
                    val errorMessage = error.message ?: "Unknown error"
                    Log.e(TAG, "Login failed: $errorMessage", error)

                    _loginState.value = when {
                        errorMessage.contains("Invalid email or password", ignoreCase = true) ||
                        errorMessage.contains("Invalid login credentials", ignoreCase = true) ||
                        errorMessage.contains("User not found", ignoreCase = true) -> {
                            LoginState.Error.InvalidCredentials(errorMessage)
                        }
                        errorMessage.contains("Unable to resolve host", ignoreCase = true) ||
                        errorMessage.contains("Failed to connect", ignoreCase = true) -> {
                            LoginState.Error.NetworkError("Network error: Please check your internet connection")
                        }
                        errorMessage.contains("Server error", ignoreCase = true) ||
                        errorMessage.contains("500", ignoreCase = true) -> {
                            LoginState.Error.ServerError("Server error: Please try again later")
                        }
                        else -> {
                            LoginState.Error.UnknownError(errorMessage)
                        }
                    }
                }
        }
    }

    fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading

            repository.register(
                firstName = firstName,
                lastName = lastName,
                login = email,
                email = email,
                password = password,
            )
                .onSuccess { loginDTO ->
                    loginDTO.token?.let { tokenManager.saveToken(it) }
                    loginDTO.refreshToken?.let { tokenManager.saveRefreshToken(it) }

                    if (loginDTO.id != null) {
                        tokenManager.saveUserInfo(loginDTO.id, firstName, lastName)
                    }

                    _registerState.value = RegisterState.Success(loginDTO)
                }
                .onFailure { error ->
                    _registerState.value = RegisterState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun googleAuth(idToken: String) {
        _googleAuthState.value = GoogleAuthState.Loading

        viewModelScope.launch {
            Log.d(TAG, "Attempting Google auth")

            repository.googleAuth(idToken)
                .onSuccess { loginDTO ->
                    Log.d(TAG, "Google auth successful")

                    loginDTO.token?.let { tokenManager.saveToken(it) }
                    loginDTO.refreshToken?.let { tokenManager.saveRefreshToken(it) }

                    if (loginDTO.id != null && loginDTO.firstName != null && loginDTO.lastName != null) {
                        tokenManager.saveUserInfo(loginDTO.id, loginDTO.firstName, loginDTO.lastName)
                    }

                    _googleAuthState.value = GoogleAuthState.Success(loginDTO)
                }
                .onFailure { error ->
                    val errorMessage = error.message ?: "Google authentication failed"
                    Log.e(TAG, "Google auth failed: $errorMessage", error)
                    _googleAuthState.value = GoogleAuthState.Error(errorMessage)
                }
        }
    }

    fun resetGoogleAuthState() {
        _googleAuthState.value = GoogleAuthState.Idle
    }

    fun requestPasswordReset(email: String) {
        _resetPasswordState.value = ResetPasswordState.RequestLoading

        viewModelScope.launch {
            repository.requestPasswordReset(email)
                .onSuccess { response ->
                    _resetPasswordState.value = ResetPasswordState.CodeSent(response.message)
                }
                .onFailure { error ->
                    _resetPasswordState.value = ResetPasswordState.Error(
                        error.message ?: "Failed to request password reset"
                    )
                }
        }
    }

    fun confirmPasswordReset(email: String, code: String, newPassword: String) {
        _resetPasswordState.value = ResetPasswordState.ConfirmLoading

        viewModelScope.launch {
            repository.confirmPasswordReset(email, code, newPassword)
                .onSuccess { response ->
                    _resetPasswordState.value = ResetPasswordState.Success(response.message)
                }
                .onFailure { error ->
                    _resetPasswordState.value = ResetPasswordState.Error(
                        error.message ?: "Failed to reset password"
                    )
                }
        }
    }

    fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }

    fun logout() {
        viewModelScope.launch {
            try {
                repository.logout()
            } catch (e: Exception) {
                Log.e(TAG, "Backend logout failed (non-critical)", e)
            }

            tokenManager.clearToken()
            resetAllStates()
            
            Log.d(TAG, "Logout completed - all states cleared")
        }
    }

    fun refreshAccessToken() {
        viewModelScope.launch {
            val refreshToken = tokenManager.getRefreshToken()
            
            if (refreshToken == null) {
                Log.w(TAG, "No refresh token available")
                return@launch
            }
            
            repository.refreshToken(refreshToken)
                .onSuccess { refreshTokenDTO ->
                    Log.d(TAG, "Token refreshed successfully")
                    refreshTokenDTO.accessToken?.let { tokenManager.saveToken(it) }
                    refreshTokenDTO.refreshToken?.let { tokenManager.saveRefreshToken(it) }
                }
                .onFailure { error ->
                    Log.e(TAG, "Token refresh failed: ${error.message}")
                    if (error.message?.contains("401") == true || error.message?.contains("Unauthorized") == true) {
                        tokenManager.clearToken()
                        resetAllStates()
                    }
                }
        }
    }

    fun resetAllStates() {
        _loginState.value = LoginState.Idle
        _registerState.value = RegisterState.Idle
        _googleAuthState.value = GoogleAuthState.Idle
        _resetPasswordState.value = ResetPasswordState.Idle
    }

    fun resetLoginState() {
        _loginState.value = LoginState.Idle
    }

    fun resetRegisterState() {
        _registerState.value = RegisterState.Idle
    }

    fun resetResetPasswordState() {
        _resetPasswordState.value = ResetPasswordState.Idle
    }

    sealed class LoginState {
        data object Idle : LoginState()
        data object Loading : LoginState()
        data class Success(val data: LoginDTO) : LoginState()

        sealed class Error(open val message: String) : LoginState() {
            data class InvalidCredentials(override val message: String) : Error(message)
            data class NetworkError(override val message: String) : Error(message)
            data class ServerError(override val message: String) : Error(message)
            data class UnknownError(override val message: String) : Error(message)
        }
    }

    sealed class RegisterState {
        data object Idle : RegisterState()
        data object Loading : RegisterState()
        data class Success(val data: LoginDTO) : RegisterState()
        data class Error(val message: String) : RegisterState()
    }

    sealed class GoogleAuthState {
        data object Idle : GoogleAuthState()
        data object Loading : GoogleAuthState()
        data class Success(val data: LoginDTO) : GoogleAuthState()
        data class Error(val message: String) : GoogleAuthState()
    }

    sealed class ResetPasswordState {
        data object Idle : ResetPasswordState()
        data object RequestLoading : ResetPasswordState()
        data object ConfirmLoading : ResetPasswordState()
        data class CodeSent(val message: String) : ResetPasswordState()
        data class Success(val message: String) : ResetPasswordState()
        data class Error(val message: String) : ResetPasswordState()
    }
}

