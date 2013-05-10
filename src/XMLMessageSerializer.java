package com.kierdavis.kmail;

import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.XMLWriter;

public class XMLMessageSerializer {
    public XMLMessageSerializer() {
        
    }
    
    public void serialize(OutputStream os, Collection<Message> msgs) throws XMLMessageSerializationException {
        Document doc = buildDocument(msgs);
        
        XMLWriter w = new XMLWriter(os);
        w.write(doc);
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
    }
    
    public void populateAddressElement(Element el, Address addr) throws XMLMessageSerializationException {
        el.addElement("username").setText(addr.getUsername());
        el.addElement("hostname").setText(addr.getHostname());
    }
}
