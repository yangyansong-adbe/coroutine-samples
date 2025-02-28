package com.yy.coroutine


import android.app.Application
import com.adobe.marketing.mobile.Edge
import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.edge.consent.Consent
import com.adobe.marketing.mobile.edge.identity.Identity

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        MobileCore.setApplication(this)
        MobileCore.setLogLevel(LoggingMode.VERBOSE)

        // The test app uses bundled config. Uncomment this and change the app ID for testing the mobile tags property.
//        MobileCore.configureWithAppID("94f571f308d5/719a9846c6c5/launch-2db90676e962-development")
//        val extensions = listOf(Edge.EXTENSION, Identity.EXTENSION, Consent.EXTENSION)
//        MobileCore.registerExtensions(extensions) {
//            val collectConsents = mutableMapOf<String, Any>()
//            collectConsents["collect"] = mutableMapOf("val" to "y")
//
//            val consents = mutableMapOf<String, Any>()
//            consents["consents"] = collectConsents
//
//            Consent.update(consents)
//
//        }
    }

}