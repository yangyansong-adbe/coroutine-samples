package com.adobe.marketing.mobile.internal.eventhub;

import kotlin.jvm.functions.Function1;

class ExtensionA extends Extension {

    ExtensionA(final ExtensionApi extensionApi) {
        super(extensionApi);
    }
    @Override
    protected void onRegistered() {
        getApi().registerEventListener("","", this::processEvent);
        Log.print("ExtensionA - onRegistered is done.");
    }
    void processEvent(final Event event){
        Log.print("ExtensionA received event: " + event.getNumber());
    }
}

class ExtensionB extends Extension {

    ExtensionB(final ExtensionApi extensionApi) {
        super(extensionApi);
    }
    @Override
    protected void onRegistered() {
        getApi().registerEventListener("","", this::processEvent);
        try {
            // IO operations: load local cached data
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Log.print("ExtensionB - onRegistered (block 1 second) is done.");
    }
    void processEvent(final Event event){
        Log.print("ExtensionB received event: " + event.getNumber());
        if (event.getNumber() == 2){
            getApi().stopEvents();
            Log.print("ExtensionB stopEvents at event 2 -------");
            BlockingOperation.sendRequest(event.getNumber(), hit -> {
                Log.print("ExtensionB: request [callback] is triggered: " + event.getNumber());
                getApi().startEvents();
                Log.print("ExtensionB startEvents at event 2 -------");
                return null;
            });
            return;
        }
        if (event.getNumber() == 3){
            Log.print("ExtensionB - 3 ");
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Log.print("ExtensionB processed event: " + event.getNumber());
    }

}

class ExtensionC extends Extension {

    private int counter = 0;
    ExtensionC(final ExtensionApi extensionApi) {
        super(extensionApi);
    }
    @Override
    protected void onRegistered() {
        getApi().registerEventListener("","", this::processEvent);
        Log.print("ExtensionC - onRegistered is done.");
    }
    void processEvent(final Event event){
        Log.print("ExtensionC received event: " + event.getNumber());
    }
    @Override
    public boolean readyForEvent(final Event event) {
        return ++this.counter > 4;
    }

}


class BlockingOperation{
    static void saveHit(final int hit){
        try {
            Thread.sleep(1000);
            Log.print("Hit saved: " + hit);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static void sendRequest(final int hit, final Function1<Integer, Void> callback){
        BlockingOperation.saveHit(hit);
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            callback.invoke(hit);
        }).start();
    }
}

