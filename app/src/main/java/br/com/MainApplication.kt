package br.com

import android.app.Application
import br.com.vansads.AdsManager

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        AdsManager.initialize(this)
    }
}