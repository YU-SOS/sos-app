package com.example.sos.user

import android.app.Application
import com.getkeepsafe.relinker.ReLinker
import com.kakao.sdk.common.KakaoSdk
import com.kakao.vectormap.KakaoMapSdk

class GlobalApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ReLinker.loadLibrary(this, "K3fAndroid")
        KakaoSdk.init(this, "68695b0d022c083d3448cec3ccbacb8a")
        KakaoMapSdk.init(this, "68695b0d022c083d3448cec3ccbacb8a")
    }
}
