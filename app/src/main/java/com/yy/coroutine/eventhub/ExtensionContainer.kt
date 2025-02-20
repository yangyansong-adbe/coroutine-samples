package com.yy.coroutine.eventhub

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

typealias EvenProcessor = (Any) -> Unit

class ExtensionContainer(
    name: String,
    events: SharedFlow<Any>,
) {
    @Volatile
    private var readyForEvent: (Any) -> Boolean = { true }
    @Volatile
    private var handleEvent: EvenProcessor = { println(it) }

    fun setReadyForEvent(readyForEvent: (Any) -> Boolean) {
        this.readyForEvent = readyForEvent
    }
    fun setHandleEvent(handleEvent: (Any) -> Unit) {
        this.handleEvent = handleEvent
    }
    // A single-threaded dispatcher for the subscriber to submit events to the queue,
    // as well as to run the processing loop.
    private val subscriberDispatcher =
        Dispatchers.IO.limitedParallelism(1, "org.example.sharedFlow.ExtensionContainer-$name")
    private val processingDispatcher =
        Dispatchers.IO.limitedParallelism(1, "org.example.sharedFlow.ExtensionContainer-$name")

    // Scope that collects from the EventBus and enqueues events.
    private val subscriberScope = CoroutineScope(subscriberDispatcher)

    // Dedicated scope for processing events sequentially.
    private val processingScope = CoroutineScope(processingDispatcher)

    // A channel to queue events. Channel.UNLIMITED is used to avoid backpressure on enqueueing.
    private val eventChannel = Channel<Any>(Channel.UNLIMITED)

    private val eventQueue: Queue<Any> = ConcurrentLinkedQueue()

    private val job: Job

    init {
        job = subscribeTo(events)
        // Launch a coroutine in the processing scope to sequentially process events from the queue.
        processingScope.launch {
            for (event in eventChannel) {
                eventQueue.add(event)
                processEventQueue()
            }
        }
    }

    @Volatile
    private var state = State.IDLE

    fun stopEvent() {
        state = State.PENDING
        job.cancel()
    }

    fun startEvent() {
        state = State.IDLE
        processingScope.launch {
            processEventQueue()
        }
    }

    private suspend fun processEventQueue() =
        withTimeoutOrNull(10000) {
            if (state == State.RUNNING) {
                return@withTimeoutOrNull
            }
            while (eventQueue.isNotEmpty()) {
                if (state == State.PENDING) {
                    return@withTimeoutOrNull
                }
                state = State.RUNNING
                // Check the event at the front of the queue.
                val candidate = eventQueue.peek() ?: return@withTimeoutOrNull
                if (readyForEvent(candidate)) {
                    handleEvent(eventQueue.poll()!!)
                } else {
                    state = State.IDLE
                    return@withTimeoutOrNull
                }
            }
            state = State.IDLE
        }

    private fun subscribeTo(events: SharedFlow<Any>) =
        subscriberScope.launch {
            events.collect { eventChannel.send(it) }
        }
}