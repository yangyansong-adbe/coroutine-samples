package com.yy.coroutine.eventhub

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

object EventHub {
    private val eventDispatcherScope =
        CoroutineScope(Dispatchers.Default.limitedParallelism(1, "EventHub"))

    // A SharedFlow to dispatch events to subscribers
    private val _events =
        MutableSharedFlow<Any>(
            replay = 0,
            extraBufferCapacity = 100,
            onBufferOverflow = BufferOverflow.SUSPEND
        )
    val events = _events.asSharedFlow()

    /** Dispatch an event to all subscribers. */
    fun dispatch(event: Any) {
        eventDispatcherScope.launch {
            // set event id
            // evaluate rules (cpu-intensive operation)
            _events.emit(event)
            println(event)
        }
    }
}
