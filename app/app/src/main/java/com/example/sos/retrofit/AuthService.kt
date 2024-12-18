package com.example.sos.retrofit

import com.example.sos.KeywordSearchResponse
import com.example.sos.res.AmbulanceRes
import com.example.sos.res.HospitalRes
import com.example.sos.Location
import com.example.sos.Page
import com.example.sos.PatientReq
import com.example.sos.res.ParamedicsRes
import com.example.sos.res.ReceptionGuestRes
import com.example.sos.res.ReceptionRes
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
    val statusCode: Int,
    val message: String,
    val accessToken: String,
    val refreshToken: String
)

data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String
)

data class AmbulanceResponse(
    val status: Int,
    val message: String,
    val data: AmbulanceRes
)

data class AmbulanceIdDupCheckResponse(
    val status: Int,
    val message: String
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

data class ReceptionRequest(
    val patient: PatientReq,
    val hospitalName: String,
    val startTime: String,
    val paramedicId: String
)

data class ReceptionResponse(
    val status: Int,
    val message: String,
    val data: String // 접수 고유 번호
)

data class LoadReceptionResponse(
    val status: Int,
    val message: String,
    val data: ReceptionRes
)

data class ParamedicModifyResponse(
    val status: Int,
    val message: String,
    val data: String? // 서버에서 반환하는 data 필드, 없으면 null로 처리
)


data class ReceptionInfoResponse(
    val status: Int,
    val message: String,
    val data: ReceptionRes
)

data class HospitalDetailResponse(
    val status: Int,
    val message: String,
    val data: HospitalRes // HospitalRes는 병원 정보를 담고 있는 데이터 클래스
)

data class HospitalLoadResponse<T>(
    val status: Int,
    val message: String,
    val data: Page<T>
)

data class ReceptionGuestResponse(
    val status: Int,
    val message: String,
    val data: ReceptionGuestRes
)

// 접수 후 재요청
data class RetryReceptionResponse(
    val status: Int,
    val message: String,
    val data: String // 새로운 receptionId
)


interface AuthService {
    @POST("/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("/signup/ambulance")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("/login/user")
    fun loginUser(@Body request: UserSignupRequest): Call<UserLoginResponse>

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
    @GET("/hospital")
    fun getHospitalList(
        @Header("Authorization") token: String,
        @Query("categories") categories: List<String>?,
        @Query("page") page: Int
    ): Call<HospitalLoadResponse<HospitalRes>>

    // 응급실 상세 조회
    @GET("/hospital/{hospitalId}")
    fun getHospitalDetails(
        @Header("Authorization") authHeader: String,
        @Path("hospitalId") hospitalId: String
    ): Call<HospitalDetailResponse>

    // Ambulance 멤버 추가
    @POST("/ambulance/{ambulanceId}/member")
    fun addAmbulanceMember(
        @Header("Authorization") token: String,
        @Path("ambulanceId") ambulanceId: String,
        @Body body: MemberRequest
    ): Call<MemberResponse>

    // 구급대 정보 조회
    @GET("/ambulance/{ambulanceId}")
    fun getAmbulanceDetails(
        @Header("Authorization") token: String,
        @Path("ambulanceId") ambulanceId: String
    ): Call<AmbulanceResponse>

    // 구급대원 정보 조회
    @GET("/ambulance/{ambulanceId}/paramedic")
    fun getParamedics(
        @Header("Authorization") token: String,
        @Path("ambulanceId") ambulanceId: String
    ): Call<ParamedicsResponse>

    // 구급대원 정보 수정
    @PUT("/ambulance/{ambulanceId}/member/{memberId}")
    fun modifyParamedic(
        @Header("Authorization") authorization: String,
        @Path("ambulanceId") ambulanceId: String,
        @Path("memberId") memberId: String,
        @Body paramedicUpdateRequest: ParamedicModifyRequest
    ): Call<ParamedicModifyResponse>

    // 구급대원 삭제
    @DELETE("/ambulance/{ambulanceId}/member/{memberId}") // 수정: ":" 제거
    fun deleteParamedic(
        @Header("Authorization") authorization: String,
        @Path("ambulanceId") ambulanceId: String,
        @Path("memberId") memberId: String
    ): Call<ParamedicDeleteResponse>

    // 응급실 접수 조회
    @GET("/reception/{receptionId}/user")
    fun getReceptionInfo(
        @Header("Authorization") authorization: String,
        @Path("receptionId") receptionId: String
    ): Call<ReceptionInfoResponse>

    // 앰뷸런스 응급실 접수 조회
    @GET("/reception/{receptionId}")
    fun getReceptionDetails(
        @Header("Authorization") token: String,
        @Path("receptionId") receptionId: String
    ): Call<LoadReceptionResponse> // 변경된 이름 사용

    // 응급실 접수 생성
    @POST("/reception")
    fun addReception(
        @Header("Authorization") token: String,
        @Body body: ReceptionRequest
    ): Call<ReceptionResponse>

    // 거절 후 재요청
    @PUT("/reception/{receptionId}/re")
    fun retryReception(
        @Header("Authorization") token: String,
        @Path("receptionId") receptionId: String,
        @Body body: Map<String, String>
    ): Call<RetryReceptionResponse>

}
interface KakaoMapService {
    @GET("/v2/local/search/keyword.json")
    suspend fun searchKeyword(
        @Header("Authorization") apiKey: String,
        @Query("query") query: String
    ): Response<KeywordSearchResponse>
}
