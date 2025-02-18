package com.yy.coroutine.eventhub

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

object Configuration {
    @Volatile
    var flag = false
    fun isConfigurationReadForEvent(event: Any): Boolean {
        return flag
    }
}

fun main() = runBlocking {
    val javaExtension = JavaExtension(ExtensionContainer("Java", EventHub.events))
    javaExtension.onRegister()

    // Create two subscribers with custom event handling logic
    ExtensionContainerV2(
        "A",
        EventHub.events,
        { event -> Configuration.isConfigurationReadForEvent(event) }) { event ->
        println("Subscriber A received event: $event")
        launch {
            val response = processHit(event)
            println("Subscriber A receive response: $response")
        }
    }

    ExtensionContainerV2("B", EventHub.events) { event ->
        println("Subscriber B start to process event: $event")
        // Simulate long-running processing
        Thread.sleep(500)
        delay(10)
        Thread.sleep(500)
        println("Subscriber B processed event: $event")
    }

    ExtensionContainerV2("C", EventHub.events) { event ->
        // Simulate wildcard listener
        println("Subscriber C received event: $event")
    }

    ExtensionContainerV2(
        "D", EventHub.events,{ true }) { event ->
        println("Subscriber D received event: $event")
    }
    // Dispatch some events
    EventHub.dispatch(1)
    EventHub.dispatch(2)
    EventHub.dispatch(3)
    EventHub.dispatch(4)
    delay(5000)
    Configuration.flag = true
    println("set Configuration flag to true")
    EventHub.dispatch(5)
    delay(2000)
}

private suspend fun processHit(hit: Any): Any {
    println("Processing hit: $hit - start")
    persistHit(hit)
    println("Processing hit: $hit - end")
    return sendNetworkRequest(hit)
}

private suspend fun sendNetworkRequest(hit: Any): Any = withContext(Dispatchers.IO) {
    delay(100)
    return@withContext hit
}

private suspend fun persistHit(hit: Any) = withContext(Dispatchers.IO) {
    delay(100)
}