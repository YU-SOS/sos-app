package com.example.sos.retrofit

import android.graphics.pdf.PdfDocument
import com.example.sos.AmbulanceRes
import com.example.sos.CategoryRes
import com.example.sos.Data
import com.example.sos.Hospital
import com.example.sos.Location
import com.example.sos.ParamedicsRes
import com.example.sos.ReceptionRes
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import java.util.Locale

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

data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String
)

data class ReceptionRequest(
    val Authorization: String,
    val receptionId: String
)

data class ReceptionResponse(
    val status: Int,
    val message: String,
    val hospitalInfo: Hospital
)

data class AmbulanceRequest(
    val name: String,
    val phoneNumber: String
)

data class AmbulanceResponse(
    val name: String
)

data class AmbulanceIdDupCheckResponse(
    val status: Int,
    val message: String
)

data class SearchHospitalResponse(
    val status: Int,
    val message: String,
    val data: Data
)

data class AmbulanceLoadResponse(
    val status: Int,
    val message: String,
    val data: AmbulanceRes,
    val imageUrl: String
)

data class MemberRequest(
    val name: String,
    val phoneNumber: String
)

data class MemberResponse(
    val status: Int,
    val message: String,
    val data: String?
)

data class ParamedicsResponse(
    val status: Int,
    val message: String,
    val data: List<ParamedicsRes>
)

data class ParamedicModifyRequest(
    val name: String,
    val phoneNumber: String
)

data class ParamedicDeleteResponse(
    val status: Int,
    val message: String,
    val data: String?
)



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

data class HospitalDetailResponse(
    val status: Int,
    val message: String,
    val id: String,
    val name: String,
    val address: String,
    val telephoneNumber: String,
    val imageUrl: String,
    val location: Location,
    val categories: List<Locale.Category>,
    val emergencyRoomStatus: String
)

data class ReceptionInfoResponse(
    val status: Int,
    val message: String,
    val data: ReceptionRes
)

interface AuthService {
    @POST("/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("/signup/ambulance")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("/login/user")
    fun loginUser(@Body request: UserSignupRequest): Call<UserLoginResponse>

    @GET("/reception/id") // 접수된 환자의 정보 요청
    fun reception(@Body request: ReceptionRequest): Call<ReceptionResponse>

    @POST("/{ambulanceId}/member") // 구급대원 추가
    fun ambulanceMember(
        @Header("Authorization") authorization: String,
        @Path("ambulanceId") ambulanceId: String,
        @Body memberInfo: AmbulanceRequest
    ): Call<AmbulanceResponse>

    @POST("/logout") // 유저 로그아웃
    fun logout(@Header("Authorization") authorization: String): Call<Void>

    @POST("/reissue-token")
    fun refreshToken(@Header("Cookie") refreshToken: String): Call<RefreshResponse>

    // ID 중복 확인
    @GET("/dup-check")
    fun checkIdDuplication(
        @Query("id") id: String,
        @Query("role") role: String
    ): Call<AmbulanceIdDupCheckResponse>

    // 응급실 목록 조회
    @GET("/hospitals/")
    fun searchHospital(
        @Header("Authorization") token: String,
        @Query("categories") categories: List<String>,
        @Query("page") page: Int
    ): Call<SearchHospitalResponse>

    // 응급실 상세 조회
    @GET("/{hospitalId}")
    fun getHospitalDetails(
        @Header("Authorization") authorization: String,
        @Path("hospitalId") hospitalId: String
    ): Call<HospitalDetailResponse>

    // Ambulance 멤버 추가
    @POST("/{ambulanceId}/member")
    fun addAmbulanceMember(
        @Header("Authorization") token: String,
        @Path("ambulanceId") ambulanceId: String,
        @Body body: MemberRequest
    ): Call<MemberResponse>

    // 구급대 정보 조회
    @GET("/{ambulanceId}")
    fun getAmbulanceMember(
        @Header("Authorization") authorization: String,
        @Path("ambulanceId") ambulanceId: String
    ): Call<AmbulanceLoadResponse>

    // 구급대원 정보 조회
    @GET("/{ambulanceId}/paramedic/paramedic")
    fun getParamedics(
        @Header("Authorization") token: String,
        @Path("ambulanceId") ambulanceId: String
    ): Call<List<ParamedicsRes>>

    // 구급대원 정보 수정
    @PUT("/ambulance/{ambulanceId}/member/{memberId}")
    fun modifyParamedic(
        @Header("Authorization") authorization: String,
        @Path("ambulanceId") ambulanceId: String,
        @Path("memberId") memberId: String,
        @Body paramedicUpdateRequest: ParamedicModifyRequest
    ): Call<Void>

    // 구급대원 삭제
    @DELETE("/{ambulanceId}/member/{memberId}") // 수정: ":" 제거
    fun deleteParamedic(
        @Header("Authorization") authorization: String,
        @Path("ambulanceId") ambulanceId: String,
        @Path("memberId") memberId: String
    ): Call<ParamedicDeleteResponse>

    // 응급실 접수 조회
    @GET("/{receptionId}")
    fun getReceptionInfo(
        @Header("Authorization") authorization: String,
        @Path("receptionId") receptionId: String
    ): Call<ReceptionInfoResponse>
}
