package com.kierdavis.kmail;

import java.io.OutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public class XMLMessageSerializer {
    private OutputFormat fmt;
    
    public XMLMessageSerializer(boolean pretty) {
        fmt = new OutputFormat();
        fmt.setIndent(pretty);
        fmt.setNewlines(pretty);
    }
    
    public void serialize(OutputStream os, Collection<Message> msgs) throws XMLMessageSerializationException {
        Document doc = buildDocument(msgs);
        
        try {
            XMLWriter w = new XMLWriter(os, fmt);
            w.write(doc);
        }
        
        catch (IOException e) {
            throw new XMLMessageSerializationException("Serialization failed: " + e.toString(), e);
        }
    }
    
    public Document buildDocument(Collection<Message> msgs) throws XMLMessageSerializationException {
        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("messages");
        
        Iterator it = msgs.iterator();
        while (it.hasNext()) {
            Message msg = (Message) it.next();
            Element msgElement = root.addElement("message");
            populateMessageElement(msgElement, msg);
        }
        
        return doc;
    }
    
    public void populateMessageElement(Element el, Message msg) throws XMLMessageSerializationException {
        populateAddressElement(el.addElement("src"), msg.getSrcAddress());
        populateAddressElement(el.addElement("dest"), msg.getDestAddress());
        
        el.addElement("body").setText(msg.getBody());
        el.addElement("sent").setText(Long.toString(msg.getSentDate().getTime()));
        
        if (msg.getReplyVia() != null) el.addElement("reply-via").setText(msg.getReplyVia());
    }
    
    public void populateAddressElement(Element el, Address addr) throws XMLMessageSerializationException {
        el.addElement("username").setText(addr.getUsername());
        el.addElement("hostname").setText(addr.getHostname());
    }
}
