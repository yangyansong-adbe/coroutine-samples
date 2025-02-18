package com.adobe.marketing.mobile.internal.eventhub.containerv2

import com.adobe.marketing.mobile.internal.eventhub.Event
import java.lang.Exception

abstract class ExtensionV2(private val extensionApi: SuspendableExtensionApi) {

    fun getApi(): SuspendableExtensionApi {
        return this.extensionApi
    }
    abstract suspend fun readyForEvent(event:Event): Boolean
    abstract suspend fun onRegistered()
}

internal fun Class<out ExtensionV2>.initWith(extensionApi: SuspendableExtensionApi): ExtensionV2? {
    try {
        val extensionConstructor = this.getDeclaredConstructor(SuspendableExtensionApi::class.java)
        extensionConstructor.isAccessible = true
        return extensionConstructor.newInstance(extensionApi)
    } catch (ex: Exception) {
        ex.printStackTrace()
    }

    return null
}
