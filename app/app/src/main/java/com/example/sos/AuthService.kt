package com.example.sos

import com.google.gson.annotations.SerializedName
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

// 카카오 로그인 요청 데이터 클래스
data class UserSignupRequest(
    val name: String,
    val providerId: String,
    val provider: String,
    val email: String
)

data class UserLoginResponse(
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("message") val message: String,
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String
)


interface AuthService {
    @POST("/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("/signup/ambulance")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("/login/user")
    fun loginUser(@Body request: UserSignupRequest): Call<UserLoginResponse>
}
