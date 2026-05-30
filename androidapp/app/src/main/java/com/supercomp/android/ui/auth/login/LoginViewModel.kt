package com.supercomp.android.ui.auth.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supercomp.android.data.local.UserPrefs
import com.supercomp.android.data.model.LoginRequest
import com.supercomp.android.data.remote.RetrofitClient
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    fun login(
        context: Context,
        email: String,
        password: String,
        onSuccess: (username: String, userId: String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    val body = response.body()!!
                    // Store token in RetrofitClient for subsequent authenticated requests
                    RetrofitClient.authToken = body.token
                    UserPrefs(context).saveSession(
                        token = body.token,
                        username = body.username,
                        userId = body.userId,
                        email = body.email,
                        profilePicture = body.profilePicture,
                        phone = body.phone,
                        city = body.city
                    )
                    onSuccess(body.username, body.userId)
                } else {
                    onError("Login fallido. Comprueba tus credenciales.")
                }
            } catch (e: Exception) {
                onError("Error de red: ${e.message}")
            }
        }
    }
}
