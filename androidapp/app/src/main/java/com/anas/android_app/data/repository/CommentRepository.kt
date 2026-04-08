package com.anas.android_app.data.repository

import android.util.Log
import com.anas.android_app.data.model.Comment
import com.anas.android_app.data.model.CommentRequest
import com.anas.android_app.data.remote.ApiService

class CommentRepository(private val api: ApiService) {

    private val TAG = "CommentRepository"

    /** READ – all comments */
    suspend fun getAllComments(): List<Comment> {
        return try {
            val response = api.getAllComments()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                Log.e(TAG, "getAllComments error: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "getAllComments exception: ${e.message}")
            emptyList()
        }
    }

    /** WRITE – post a new comment */
    suspend fun postComment(name: String, message: String): Boolean {
        return try {
            val response = api.postComment(CommentRequest(name = name, message = message))
            if (response.isSuccessful) {
                Log.d(TAG, "Comment posted: ${response.body()?.message}")
                true
            } else {
                Log.e(TAG, "postComment error: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "postComment exception: ${e.message}")
            false
        }
    }
}
