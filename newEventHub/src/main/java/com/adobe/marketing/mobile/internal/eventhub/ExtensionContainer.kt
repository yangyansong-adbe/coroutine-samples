package com.adobe.marketing.mobile.internal.eventhub

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

typealias EvenProcessor = (Event) -> Unit

class ExtensionContainer(
    name: String,
    extensionClass: Class<out Extension>,
    events: SharedFlow<Event>,
) : ExtensionApi {

    @Volatile
    private var handleEvent: EvenProcessor = { println(it) }

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

    private val extension: Extension = extensionClass.initWith(this)!!

    init {
        job = subscribeTo(events)
        // Launch a coroutine in the processing scope to sequentially process events from the queue.
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
            // TODO: when timeout reached, retry processing the event may run into issues. Consider remove the timeout.
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
        eventListener: ExtensionEventListener
    ) {
        this.handleEvent = { eventListener.hear(it) }
    }

    override fun dispatch(event: Event) {
        EventHub.dispatch(event)
    }

    override fun startEvents() {
        if(state == State.PENDING){
            state = State.IDLE
        }
        processingScope.launch {
            processEventQueue()
        }
    }

    override fun stopEvents() {
        state = State.PENDING
    }

}