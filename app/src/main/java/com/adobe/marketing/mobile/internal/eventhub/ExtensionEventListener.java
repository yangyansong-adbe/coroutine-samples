package com.adobe.marketing.mobile.internal.eventhub;

@FunctionalInterface
public interface ExtensionEventListener {
    void hear(final Event event);
}