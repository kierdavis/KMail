package com.kierdavis.kmail;

public class XMLMessageSerializationException extends Exception {
    public XMLMessageSerializationException(String message) {
        super(message);
    }
    
    public XMLMessageSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
