package com.yy.coroutine.eventhub;

class JavaExtension {
    private final ExtensionContainer extensionContainer;
    JavaExtension(ExtensionContainer extensionContainer) {
        this.extensionContainer = extensionContainer;
    }
    void onRegister() {
        this.extensionContainer.setHandleEvent(event ->{
            System.out.println("JavaExtension received event: " + event);
            return null;
        });
        this.extensionContainer.setReadyForEvent(event -> {
            return true;
        });
    }


}
