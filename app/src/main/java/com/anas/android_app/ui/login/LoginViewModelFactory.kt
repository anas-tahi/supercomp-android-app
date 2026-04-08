package com.anas.android_app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anas.android_app.data.local.UserPrefs
import com.anas.android_app.data.remote.ApiService

class LoginViewModelFactory(
    private val prefs: UserPrefs,
    private val api: ApiService
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginViewModel(prefs, api) as T
    }
}
