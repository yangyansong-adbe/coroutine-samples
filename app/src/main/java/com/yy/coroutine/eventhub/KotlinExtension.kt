package com.yy.coroutine.eventhub

import kotlinx.coroutines.flow.SharedFlow

internal class KotlinExtension(name: String, events: SharedFlow<Any>) {
    val containerV2 = ExtensionContainerV2(name, events) { event ->
        // Simulate wildcard listener
        println("Subscriber A received event: $event")
    }

}