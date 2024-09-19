package com.example.sos

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val appKey = getString(R.string.kakao_app_key)
        // Kakao Sdk 초기화
        KakaoSdk.init(this, "68695b0d022c083d3448cec3ccbacb8a")
    }
}
