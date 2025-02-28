package com.adobe.marketing.mobile.internal.eventhub

import java.lang.Exception

internal fun Class<out Extension>.initWith(extensionApi: ExtensionApi): Extension? {
    try {
        val extensionConstructor = this.getDeclaredConstructor(ExtensionApi::class.java)
        extensionConstructor.isAccessible = true
        return extensionConstructor.newInstance(extensionApi)
    } catch (ex: Exception) {
        ex.printStackTrace()
    }

    return null
}