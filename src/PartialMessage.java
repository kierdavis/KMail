package com.kierdavis.kmail;

public class PartialMessage {
    private Message msg;
    private StringBuilder bodyBuilder;
    
    public PartialMessage(Message msg_) {
        msg = msg_;
        bodyBuilder = new StringBuilder();
    }
    
    public synchronized void append(String s) {
        bodyBuilder.append(s);
        bodyBuilder.append("\n");
    }
    
    public synchronized Message finish() {
        msg.setBody(bodyBuilder.toString().trim());
        return msg;
    }
}
