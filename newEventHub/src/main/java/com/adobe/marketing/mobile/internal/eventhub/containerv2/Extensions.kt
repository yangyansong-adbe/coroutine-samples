package com.adobe.marketing.mobile.internal.eventhub.containerv2

import com.adobe.marketing.mobile.internal.eventhub.Event
import com.adobe.marketing.mobile.internal.eventhub.Log
import kotlinx.coroutines.delay

internal class ExtensionA(extensionApi: SuspendableExtensionApi) : ExtensionV2(extensionApi) {
    override suspend fun readyForEvent(event: Event): Boolean {
        return true;
    }

    override suspend fun onRegistered() {
        getApi().registerEventListener("",""){ event ->
            Log.print("ExtensionA received event: " + event.getNumber())
        }
        Log.print("ExtensionA - onRegistered is done.")
    }
}

internal class ExtensionB(extensionApi: SuspendableExtensionApi) : ExtensionV2(extensionApi) {
    override suspend fun readyForEvent(event: Event): Boolean {
        return true;
    }

    override suspend fun onRegistered() {
        getApi().registerEventListener("",""){ event ->
            Log.print("ExtensionB received event: " + event.getNumber())
            NonBlockingOperation.sendRequest(event.getNumber())
            Log.print("ExtensionB processed event: " + event.getNumber())
        }
        Thread.sleep(1000)
        Log.print("ExtensionB - onRegistered (block 1 second) is done.")
    }
}

internal class ExtensionC(extensionApi: SuspendableExtensionApi) : ExtensionV2(extensionApi) {
    override suspend fun readyForEvent(event: Event): Boolean {
        val eventNumberLessThanFive = getApi().getSharedState(event.getNumber())
        if(eventNumberLessThanFive == null){
            return false
        }else {
            return true;
        }
    }

    override suspend fun onRegistered() {
        getApi().registerEventListener("",""){ event ->
            Log.print("ExtensionC received event: " + event.getNumber())
        }
        Log.print("ExtensionC - onRegistered is done.")
    }
}


internal object NonBlockingOperation {
    private suspend fun saveHit(hit: Int) {
        when(hit){
            1 -> delay(2000)
            2 -> delay(1000)
            3 -> delay(500)
            4 -> delay(500)
            5 -> delay(500)
        }
    }

    suspend fun sendRequest(hit: Int): Any {
        saveHit(hit)
        // send request
        delay(100)
        return {}
    }
}