package com.example.sos

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(
    val id: String,
    val password: String
)

data class LoginResponse(
    val token: String
)

data class RegisterRequest(
    val id: String,
    val password: String,
    val name: String,
    val phoneNumber: String,
    val address: String,
    val longitude: String,
    val latitude: String,
    val imageURL: String
)

data class RegisterResponse(
    val success: Boolean,
    val message: String
)

interface AuthService {
    @POST("/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("/signup/ambulance")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>
}
