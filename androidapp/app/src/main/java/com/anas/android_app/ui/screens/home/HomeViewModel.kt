package com.anas.android_app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anas.android_app.data.model.CommentRequest
import com.anas.android_app.data.remote.ApiServiceInstance
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    fun sendComment(username: String, message: String) {
        viewModelScope.launch {
            try {
                ApiServiceInstance.api.sendComment(
                    CommentRequest(username, message)
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
