package com.example.loginproject

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        KakaoSdk.init(this, "d65e0a3cba00f92c7a9d9441e5d41bf8")
    }
}