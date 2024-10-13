package com.example.sos.user

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        KakaoSdk.init(this, "68695b0d022c083d3448cec3ccbacb8a")
    }
}
