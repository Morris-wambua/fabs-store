package com.morrislabs.fabs_store.ui.screens

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.morrislabs.fabs_store.R
import com.morrislabs.fabs_store.ui.components.ErrorDialog
import com.morrislabs.fabs_store.ui.viewmodel.AuthViewModel

@Composable
fun ResetPasswordScreen(
    onNavigateBack: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf<String?>(null) }
    var codeError by remember { mutableStateOf<String?>(null) }
    var newPasswordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val resetState by authViewModel.resetPasswordState.collectAsState()
    val isCodeStep = resetState is AuthViewModel.ResetPasswordState.CodeSent ||
        resetState is AuthViewModel.ResetPasswordState.ConfirmLoading ||
        resetState is AuthViewModel.ResetPasswordState.Success

    fun validateEmail(): Boolean {
        return when {
            email.isBlank() -> {
                emailError = "Email is required"
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                emailError = "Please enter a valid email"
                false
            }
            else -> {
                emailError = null
                true
            }
        }
    }

    fun validatePassword(): Boolean {
        val hasMinLength = newPassword.length >= 8
        val hasUpper = newPassword.any { it.isUpperCase() }
        val hasLower = newPassword.any { it.isLowerCase() }
        val hasDigit = newPassword.any { it.isDigit() }
        val hasSpecial = newPassword.any { !it.isLetterOrDigit() }

        if (code.isBlank()) {
            codeError = "Reset code is required"
            return false
        }
        codeError = null

        if (!(hasMinLength && hasUpper && hasLower && hasDigit && hasSpecial)) {
            newPasswordError = "Use 8+ chars with upper, lower, number, and symbol"
            return false
        }
        newPasswordError = null

        if (confirmPassword != newPassword) {
            confirmPasswordError = "Passwords do not match"
            return false
        }
        confirmPasswordError = null

        return true
    }

    LaunchedEffect(resetState) {
        when (val state = resetState) {
            is AuthViewModel.ResetPasswordState.Error -> {
                errorMessage = state.message
                showErrorDialog = true
            }
            else -> Unit
        }
    }

    if (showErrorDialog) {
        ErrorDialog(
            errorMessage = errorMessage,
            onDismiss = { showErrorDialog = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IconButton(
            onClick = {
                authViewModel.resetResetPasswordState()
                onNavigateBack()
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
        }

        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .align(Alignment.Center)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Fabs Store Logo",
                modifier = Modifier
                    .size(100.dp)
                    .padding(bottom = 16.dp)
            )

            Text(
                text = if (resetState is AuthViewModel.ResetPasswordState.Success) "Password Reset Successful" else "Reset Password",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = when (val state = resetState) {
                    is AuthViewModel.ResetPasswordState.Success -> state.message
                    is AuthViewModel.ResetPasswordState.CodeSent -> state.message
                    else -> "Enter your email to receive a reset code, then set a new password."
                },
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (resetState !is AuthViewModel.ResetPasswordState.Success) {
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = null
                    },
                    label = { Text("Email") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Email, contentDescription = "Email")
                    },
                    singleLine = true,
                    isError = emailError != null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (emailError != null) 4.dp else 16.dp)
                )

                if (emailError != null) {
                    Text(
                        text = emailError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, bottom = 12.dp)
                    )
                }
            }

            if (isCodeStep && resetState !is AuthViewModel.ResetPasswordState.Success) {
                OutlinedTextField(
                    value = code,
                    onValueChange = {
                        code = it
                        codeError = null
                    },
                    label = { Text("Reset Code") },
                    singleLine = true,
                    isError = codeError != null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (codeError != null) 4.dp else 16.dp)
                )

                if (codeError != null) {
                    Text(
                        text = codeError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, bottom = 12.dp)
                    )
                }

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        newPasswordError = null
                    },
                    label = { Text("New Password") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "New Password")
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    isError = newPasswordError != null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (newPasswordError != null) 4.dp else 16.dp)
                )

                if (newPasswordError != null) {
                    Text(
                        text = newPasswordError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, bottom = 12.dp)
                    )
                }

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        confirmPasswordError = null
                    },
                    label = { Text("Confirm Password") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Confirm Password")
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    isError = confirmPasswordError != null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (confirmPasswordError != null) 4.dp else 16.dp)
                )

                if (confirmPasswordError != null) {
                    Text(
                        text = confirmPasswordError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, bottom = 12.dp)
                    )
                }
            }

            if (resetState is AuthViewModel.ResetPasswordState.Success) {
                Button(
                    onClick = {
                        authViewModel.resetResetPasswordState()
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Return to Login", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                }
            } else {
                Button(
                    onClick = {
                        if (!isCodeStep) {
                            if (validateEmail()) {
                                authViewModel.requestPasswordReset(email)
                            }
                        } else {
                            if (validateEmail() && validatePassword()) {
                                authViewModel.confirmPasswordReset(email, code, newPassword)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    enabled = resetState !is AuthViewModel.ResetPasswordState.RequestLoading &&
                        resetState !is AuthViewModel.ResetPasswordState.ConfirmLoading
                ) {
                    if (resetState is AuthViewModel.ResetPasswordState.RequestLoading ||
                        resetState is AuthViewModel.ResetPasswordState.ConfirmLoading
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(
                            text = if (!isCodeStep) "Send Reset Code" else "Reset Password",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            if (resetState !is AuthViewModel.ResetPasswordState.Success) {
                TextButton(
                    onClick = {
                        authViewModel.resetResetPasswordState()
                        onNavigateBack()
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Back to Login", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
