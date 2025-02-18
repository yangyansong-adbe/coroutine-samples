package com.adobe.marketing.mobile.internal.eventhub

interface ExtensionApi {
    fun registerEventListener(
        eventType: String,
        eventSource: String,
        eventListener: ExtensionEventListener
    )

    fun dispatch(event: Event)
    fun startEvents()
    fun stopEvents()
}