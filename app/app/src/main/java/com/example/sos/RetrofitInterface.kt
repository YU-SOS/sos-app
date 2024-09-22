package com.example.sos

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface API {
        @GET("/posts")
        fun getData(@Query("accessToken") accessToken: String): Call<List<PostData>>

        @Headers("Content-Type: application/json")
        @POST("/api/auth/token")  // 서버의 경로로 수정 필요
        fun sendToken(@Body tokenData: PostData): Call<Void>
    }