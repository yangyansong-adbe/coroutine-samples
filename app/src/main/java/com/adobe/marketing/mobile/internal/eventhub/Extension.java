package com.adobe.marketing.mobile.internal.eventhub;

public abstract class Extension {

    private final ExtensionApi extensionApi;

    protected Extension(final ExtensionApi extensionApi) {
        this.extensionApi = extensionApi;
    }

    protected abstract void onRegistered();

    public boolean readyForEvent(final Event event) {
        return true;
    }

    public final ExtensionApi getApi() {
        return extensionApi;
    }

}