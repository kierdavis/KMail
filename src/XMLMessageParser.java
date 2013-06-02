package com.kierdavis.kmail;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.io.File;
import java.io.InputStream;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class XMLMessageParser {
    private boolean isMailbox;
    
    public XMLMessageParser(boolean isMailbox_) {
        isMailbox = isMailbox_;
    }
    
    public List<Message> parse(InputStream is) throws XMLMessageParseException {
        SAXReader reader = new SAXReader();
        Document doc;
        
        try {
            doc = reader.read(is);
        }
        catch (DocumentException e) {
            throw new XMLMessageParseException("Could not parse document: " + e.toString(), e);
        }
        
        return parse(doc);
    }
    
    public List<Message> parse(File file) throws XMLMessageParseException {
        SAXReader reader = new SAXReader();
        Document doc;
        
        try {
            doc = reader.read(file);
        }
        catch (DocumentException e) {
            throw new XMLMessageParseException("Could not parse document: " + e.toString(), e);
        }
        
        return parse(doc);
    }
    
    public List<Message> parse(Document doc) throws XMLMessageParseException {
        List<Message> msgs = new ArrayList<Message>();
        Element root = doc.getRootElement();
        Iterator it = root.elementIterator("message");
        
        while (it.hasNext()) {
            Element el = (Element) it.next();
            Message msg = parseMessage(el);
            
            if (msg != null) {
                msgs.add(msg);
            }
        }
        
        return msgs;
    }
    
    public Message parseMessage(Element el) throws XMLMessageParseException {
        Message msg = new Message();
        
        Element srcEl = el.element("src");
        if (srcEl == null) {
            throw new XMLMessageParseException("Invalid or missing value for <src> element in <message>");
        }
        msg.setSrcAddress(parseAddress(srcEl));
        
        Element destEl = el.element("dest");
        if (destEl == null) {
            throw new XMLMessageParseException("Invalid or missing value for <dest> element in <message>");
        }
        msg.setDestAddress(parseAddress(destEl));
        
        String sentStr = el.elementTextTrim("sent");
        long sentTime;
        
        try {
            sentTime = Long.parseLong(sentStr);
        }
        catch (NumberFormatException e) {
            throw new XMLMessageParseException("Invalid or missing value for <sent> element in <message>", e);
        }
        
        msg.setBody(el.elementText("body"));
        msg.setReplyVia(el.elementTextTrim("reply-via"));
        msg.setSentDate(new Date(sentTime));
        
        if (isMailbox) {
            String receivedStr = el.elementTextTrim("received");
            long receivedTime;
            
            try {
                receivedTime = Long.parseLong(receivedStr);
            }
            catch (NumberFormatException e) {
                throw new XMLMessageParseException("Invalid or missing value for <received> element in <message>", e);
            }
            
            msg.setReceivedDate(new Date(receivedTime));
            
            Element tagsEl = el.element("tags");
            if (tagsEl != null) {
                List<Element> tagEls = tagsEl.elements("tag");
                
                for (int i = 0; i < tagEls.size(); i++) {
                    Element tagEl = tagEls.get(i);
                    msg.addTag(tagEl.getTextTrim());
                }
            }
        }
        
        return msg;
    }
    
    public Address parseAddress(Element el) throws XMLMessageParseException {
        String username = el.elementTextTrim("username");
        if (username == null || username.length() == 0) {
            throw new XMLMessageParseException("Invalid or missing value for <username> element in <src>/<dest>");
        }
        
        String hostname = el.elementTextTrim("hostname");
        if (hostname == null || hostname.length() == 0) {
            throw new XMLMessageParseException("Invalid or missing value for <hostname> element in <src>/<dest>");
        }
        
        return new Address(username, hostname);
    }
}
