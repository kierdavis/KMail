package com.kierdavis.kmail;

public class PartialMessage {
    private Message msg;
    private StringBuilder bodyBuilder;
    
    public PartialMessage(Message msg) {
        this.msg = msg;
        this.bodyBuilder = new StringBuilder();
    }
    
    public void append(String s) {
        bodyBuilder.append(s);
        bodyBuilder.append(" ");
    }
    
    public Message finish() {
        msg.setBody(bodyBuilder.toString());
        return msg;
    }
}
