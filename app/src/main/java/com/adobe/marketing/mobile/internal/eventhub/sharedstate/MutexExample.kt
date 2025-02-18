package com.adobe.marketing.mobile.internal.eventhub.sharedstate

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.measureTimedValue

private val mutex = Mutex()
private var counterWithMutex = 0
private var counterNoMutex = 0

fun main() = runBlocking {
    val scope = CoroutineScope(Dispatchers.IO)
    val (_, duration1) = measureTimedValue{
        scope.launch {
            for (i in 1..500) {
                incrementCounterByTenSynchronized()
            }
        }.join()

        scope.launch {
            for (i in 1..500) {
                incrementCounterByTenSynchronized()
            }
        }.join()
    }


    val (_, duration2) = measureTimedValue{
        scope.launch {
            for (i in 1..500) {
                incrementCounterByTenWithMutex()
            }
        }

        scope.launch {
            for (i in 1..500) {
                incrementCounterByTenWithMutex()
            }
        }
    }

    println("$duration1: $duration2")
    //6.477583ms: 1.523625ms
}


private suspend fun incrementCounterByTenWithMutex() {
    mutex.withLock {
        for (i in 0 until 10) {
            counterWithMutex++
        }
    }
}

@Synchronized
private fun incrementCounterByTenSynchronized() {
    for (i in 0 until 10) {
        counterNoMutex++
    }
}