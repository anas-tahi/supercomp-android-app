package com.supercomp.android.ui.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supercomp.android.data.model.RegisterRequest
import com.supercomp.android.data.remote.RetrofitClient
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    fun register(
        username: String,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.register(RegisterRequest(username, email, password))
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("Registration failed. Email may already be in use.")
                }
            } catch (e: Exception) {
                onError("Network error: ${e.message}")
            }
        }
    }
}
