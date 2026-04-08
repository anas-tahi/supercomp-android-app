package com.anas.android_app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anas.android_app.data.local.UserPrefs
import com.anas.android_app.data.model.LoginRequest
import com.anas.android_app.data.remote.ApiService
import kotlinx.coroutines.launch

class LoginViewModel(
    private val prefs: UserPrefs,
    private val api: ApiService
) : ViewModel() {

    fun login(
        email: String,
        password: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = api.login(LoginRequest(email, password))

                if (response.isSuccessful) {
                    val body = response.body()!!
                    prefs.saveToken(body.token)
                    prefs.saveUsername(body.username)
                    onSuccess(body.username)
                } else {
                    onError("Invalid credentials")
                }

            } catch (e: Exception) {
                onError("Network error")
            }
        }
    }
}
