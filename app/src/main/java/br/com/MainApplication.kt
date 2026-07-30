package br.com

import android.app.Application
import br.com.vansads.AdsManager
import br.com.vansads.BillingManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        CoroutineScope(Dispatchers.IO).launch {
            // Read the entitlement first, so the ads never start for a paying user.
            BillingManager.initialize(this@MainApplication)
            AdsManager.initialize(this@MainApplication)
        }
    }
}