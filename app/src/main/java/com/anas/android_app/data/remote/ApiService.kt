package com.anas.android_app.data.remote

import com.anas.android_app.data.model.LoginRequest
import com.anas.android_app.data.model.LoginResponse
import com.anas.android_app.data.model.CommentRequest
import com.anas.android_app.data.model.BasicResponse
import com.anas.android_app.data.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<BasicResponse>

    @POST("/auth/comment")
    suspend fun sendComment(
        @Body request: CommentRequest
    ): Response<BasicResponse>
}
