package com.example.sos.retrofit

import com.example.sos.Hospital
import com.example.sos.Location
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

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
    val status: Int,
    val message: String
)

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

data class RefreshRequest(
    val refreshToken: String
)

data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String
)
//환자 정보 요청(수정중)
data class ReceptionRequest(
    val Authorization: String,
    val receptionId: String
)
//환자 정보 응답(수정중)
data class ReceptionResponse(
    val status: Int,
    val message: String,
    val hospitalInfo: Hospital,

    )

data class AmbulanceRequest(//수정중
    val name: String,
    val phoneNumber: String
)

data class AmbulanceResponse(//수정중
    val status: Int,
    val message: String
)

interface AuthService {
    @POST("/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("/signup/ambulance")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("/login/user")
    fun loginUser(@Body request: UserSignupRequest): Call<UserLoginResponse>

    @GET("/reissue-token")
    fun refreshToken(@Header("Cookie") refreshToken: String): Call<RefreshResponse>

    @GET("/reception/id")//접수된 환자의 정보 요청(수정해야됨)
    fun reception(@Body request: ReceptionRequest): Call<ReceptionResponse>

    @POST("/{ambulanceId}/member")//수정해야됨
    fun ambulanceMember(
        @Header("Authorization") authorization: String,
        @Path("ambulanceId") ambulanceId: String,
        @Body memberInfo: AmbulanceRequest
    ): Call<AmbulanceResponse>

    @POST("/logout") // 유저 로그아웃
    fun logout(@Header("Authorization") authorization: String): Call<Void>
}

// 카카오 주소 검색 이용 관련
interface KakaoMapService {
    @GET("/v2/local/search/address.json")
    suspend fun searchAddress(
        @Header("Authorization") apiKey: String,
        @Query("query") query: String
    ): Response<AddressSearchResponse>
}

data class AddressSearchResponse(
    val documents: List<Document>
)

data class Document(
    val address_name: String,  // 주소명
    val x: String,  // 경도 (Longitude)
    val y: String   // 위도 (Latitude)
)

