package com.example.sos

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(
    val role: String,
    val id: String,
    val password: String
)

data class LoginResponse(
    val status: Int,
    val message: String,
    val data: String?
)

data class RegisterRequest(
    val id: String,
    val password: String,
    val name: String,
    val address: String,
    val telephoneNumber: String,
    val location: Location,
    val imageUrl: String
)

data class RegisterResponse(
    val statusCode: String,
    val message: String
)

interface AuthService {
    @POST("/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("/signup/ambulance")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>
}
