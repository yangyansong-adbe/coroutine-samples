package com.adobe.marketing.mobile.internal.eventhub

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {

    ExtensionContainer("A", ExtensionA::class.java, EventHub.events)
    // ExtensionA only prints the event number
    ExtensionContainer("B", ExtensionB::class.java, EventHub.events)
    // ExtensionB processes each event in 1 second to simulate cpu-intensive operations or other IO-related blocking operations
    ExtensionContainer("C", ExtensionC::class.java, EventHub.events)
    // ExtensionC will not be 'readyForEvent' for the first four events; they should be queued.


    EventHub.dispatch(Event(1))
    EventHub.dispatch(Event(2))
    EventHub.dispatch(Event(3))
    EventHub.dispatch(Event(4))

    delay(5000)

    // ExtensionC will be "readyForEvent" after receiving 5 events and start to process queued events.
    println("Event 5 will unblock Extension C")

    EventHub.dispatch(Event(5))

    delay(2000)
}