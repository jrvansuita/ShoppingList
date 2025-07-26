package br.com

import android.app.Application
import br.com.vansads.AdsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        CoroutineScope(Dispatchers.IO).launch {
            AdsManager.initialize(this@MainApplication)
        }
    }
}