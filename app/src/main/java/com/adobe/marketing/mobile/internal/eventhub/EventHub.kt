package com.adobe.marketing.mobile.internal.eventhub

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

object EventHub {
    private val eventHubExecutorScope =
        CoroutineScope(Dispatchers.Default.limitedParallelism(1, "EventHub"))
    private val eventDispatcherScope =
        CoroutineScope(Dispatchers.Default.limitedParallelism(1, "EventHub"))

    // TODO: handle response events within this scope
    private val responseHandlerScope =
        CoroutineScope(Dispatchers.Default.limitedParallelism(1, "ResponseEventHandler"))

    // A SharedFlow to dispatch events to subscribers
    private val _events =
        MutableSharedFlow<Event>(
            replay = 0,
            extraBufferCapacity = 100,
            onBufferOverflow = BufferOverflow.SUSPEND
        )
    val events = _events.asSharedFlow()
    private val eventPreprocessorsChannel = Channel<Event>(Channel.UNLIMITED)

    init {
        eventDispatcherScope.launch {
            for (event in eventPreprocessorsChannel) {
                //TODO: execute rulesEngine.processEvent()
                _events.emit(event)
            }
        }
    }

    /** Dispatch an event to all subscribers. */
    fun dispatch(event: Event) {
        eventHubExecutorScope.launch {
            eventPreprocessorsChannel.send(event)
        }
    }
}
