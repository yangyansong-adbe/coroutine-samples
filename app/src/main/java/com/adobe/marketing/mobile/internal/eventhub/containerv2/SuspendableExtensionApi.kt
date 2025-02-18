package com.adobe.marketing.mobile.internal.eventhub.containerv2

import com.adobe.marketing.mobile.internal.eventhub.Event

interface SuspendableExtensionApi {
    fun registerEventListener(
        eventType: String,
        eventSource: String,
        eventProcessor: EvenProcessor
    )

    fun dispatch(event: Event)
    suspend fun getSharedState(count: Int): Any?
    suspend fun createSharedState(data: String)
}