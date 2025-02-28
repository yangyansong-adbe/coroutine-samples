package com.adobe.marketing.mobile.internal.eventhub.containerv2

import com.adobe.marketing.mobile.internal.eventhub.Event
import com.adobe.marketing.mobile.internal.eventhub.EventHub
import com.adobe.marketing.mobile.internal.eventhub.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

typealias EvenProcessor = suspend (Event) -> Unit


class SuspendableExtensionContainer(
    name: String,
    extensionClass: Class<out ExtensionV2>,
    events: SharedFlow<Event>,
) : SuspendableExtensionApi {

    @Volatile
    private var handleEvent: EvenProcessor = { println(it) }

    private val mutex = Mutex()

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
    private val eventChannel = Channel<Event>(Channel.UNLIMITED)

    private val eventQueue: Queue<Event> = ConcurrentLinkedQueue()

    private val job: Job

    private val extension: ExtensionV2 = extensionClass.initWith(this)!!

    init {
        job = subscribeTo(events)

        processingScope.launch {
            state = State.RUNNING
            extension.onRegistered()
            if (state == State.RUNNING) {
                state = State.IDLE
            }
        }
        processingScope.launch {
            for (event in eventChannel) {
                eventQueue.add(event)
//                Log.print("$name queue size: ${eventQueue.size}")
                processEventQueue()
            }
        }

    }

    @Volatile
    private var state = State.IDLE

    private suspend fun processEventQueue() =
//        withTimeoutOrNull(10000) {
        coroutineScope {
            if (state == State.RUNNING) {
                return@coroutineScope
            }
            while (eventQueue.isNotEmpty()) {
                if (state == State.PENDING) {
                    return@coroutineScope
                }
                state = State.RUNNING
                // Check the event at the front of the queue.
                val candidate = eventQueue.peek() ?: return@coroutineScope
                if (extension.readyForEvent(candidate)) {
                    handleEvent(eventQueue.poll()!!)
                } else {
                    state = State.IDLE
                    return@coroutineScope
                }
            }
            if (state == State.RUNNING) {
                state = State.IDLE
            }
        }

    private fun subscribeTo(events: SharedFlow<Event>) =
        subscriberScope.launch {
            events.collect { eventChannel.send(it) }
        }

    override fun registerEventListener(
        eventType: String,
        eventSource: String,
        eventProcessor: EvenProcessor
    ) {
        this.handleEvent = eventProcessor
    }

    override fun dispatch(event: Event) {
        EventHub.dispatch(event)
    }
    private val list = mutableSetOf<String>()

    private var counter = 0
    override suspend fun getSharedState(count: Int): String? {
        mutex.withLock {
            if (++counter < 5) return null
            return ""
        }
    }

    override suspend fun createSharedState(data: String) {
        mutex.withLock {
            list.add(data)
        }
    }

}