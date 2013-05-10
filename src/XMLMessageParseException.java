package com.kierdavis.kmail;

public class XMLMessageParseException extends Exception {
    public XMLMessageParseException(String message) {
        super(message);
    }
    
    public XMLMessageParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
