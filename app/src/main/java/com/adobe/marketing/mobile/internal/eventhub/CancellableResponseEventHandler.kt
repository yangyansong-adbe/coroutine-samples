package com.adobe.marketing.mobile.internal.eventhub

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


typealias ResponseEventHandler = (Event?) -> Unit

object CancellableResponseEventHandler {
    private val coroutineScope = CoroutineScope(Dispatchers.IO.limitedParallelism(1))
    private val map = mutableMapOf<String, Job>()
    private val handlerMap = mutableMapOf<String, ResponseEventHandler>()

    fun scheduleTimeoutHandler(
        uuid: String,
        timeoutInMilliseconds: Long,
        handler: ResponseEventHandler
    ) {
        val job = scheduleTimeoutJob(timeoutInMilliseconds, handler)
        map[uuid] = job
        handlerMap[uuid] = handler
    }

    fun executeHandler(uuid: String, event: Event) {
        map.remove(uuid)?.cancel()
        handlerMap[uuid]?.invoke(event)
    }


    private fun scheduleTimeoutJob(
        timeoutInMilliseconds: Long,
        handler: ResponseEventHandler
    ): Job {
        return coroutineScope.launch {
            delay(timeoutInMilliseconds)
            handler(null)
        }
    }

}

fun main() = runBlocking {
    CancellableResponseEventHandler.scheduleTimeoutHandler("1", 200, { event ->
        if (event != null) {
            println("Handle response event - 1")
        } else {
            println("Timeout - 1")
        }
    })
    CancellableResponseEventHandler.scheduleTimeoutHandler("2", 500, { event ->
        if (event != null) {
            println("Handle response event - 2")
        } else {
            println("Timeout - 2")
        }
    })
    delay(100)
    CancellableResponseEventHandler.executeHandler("2", Event(1))
    delay(1000)
}
