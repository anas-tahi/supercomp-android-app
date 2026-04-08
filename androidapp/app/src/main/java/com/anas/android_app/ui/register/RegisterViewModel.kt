package com.anas.android_app.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anas.android_app.data.model.RegisterRequest
import com.anas.android_app.data.remote.ApiServiceInstance
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
                val response = ApiServiceInstance.api.register(
                    RegisterRequest(username, email, password)
                )

                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("Registration failed")
                }

            } catch (e: Exception) {
                onError("Network error")
            }
        }
    }
}
