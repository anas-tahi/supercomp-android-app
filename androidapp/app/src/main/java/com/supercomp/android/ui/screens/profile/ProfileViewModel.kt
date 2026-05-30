package com.supercomp.android.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supercomp.android.data.model.UpdateProfileRequest
import com.supercomp.android.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun clearMessages() { _successMessage.value = null; _errorMessage.value = null }

    fun updateProfile(
        userId: String,
        newUsername: String?,
        newPhone: String?,
        newCity: String?,
        newProfilePicture: String?,
        currentPassword: String?,
        newPassword: String?,
        onProfileUpdated: (String?, String?, String?, String?) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = UpdateProfileRequest(
                    username = newUsername?.takeIf { it.isNotBlank() },
                    phone = newPhone?.takeIf { it.isNotBlank() },
                    city = newCity?.takeIf { it.isNotBlank() },
                    profilePicture = newProfilePicture,
                    currentPassword = currentPassword?.takeIf { it.isNotBlank() },
                    newPassword = newPassword?.takeIf { it.isNotBlank() }
                )
                val r = RetrofitClient.api.updateProfile(userId, request)
                if (r.isSuccessful) {
                    val body = r.body()
                    onProfileUpdated(
                        body?.username,
                        body?.profilePicture,
                        body?.phone,
                        body?.city
                    )
                    _successMessage.value = "Profile updated successfully!"
                } else {
                    _errorMessage.value = "Update failed. Check your current password."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAccount(
        userId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val r = RetrofitClient.api.deleteAccount(userId)
                if (r.isSuccessful) {
                    onSuccess()
                } else {
                    onError("Failed to delete account. Please try again.")
                }
            } catch (e: Exception) {
                onError("Network error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
