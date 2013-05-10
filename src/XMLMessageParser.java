package com.kierdavis.kmail;

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.doc.Document;
import org.w3c.doc.Element;
import org.w3c.doc.NodeList;

public class XMLMessageParser {
    private DocumentBuilderFactory factory;
    
    public XMLMessageParser() {
        factory = DocumentBuilderFactory.newInstance();
    }
    
    public List<Message> parse(InputStream is) throws XMLMessageParseException {
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(is);
        
        return parse(doc);
    }
    
    public List<Message> parse(File file) throws XMLMessageParseException {
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(file);
        
        return parse(doc);
    }
    
    public List<Message> parse(Document doc) throws XMLMessageParseException {
        List<Message> msgs = new ArrayList<Message>();
        Element root = doc.getDocumentElement();
        NodeList messageNodes = root.getElementsByTagName("message");
        
        if (messageNodes == null || messageNodes.getLength() == 0) {
            return msgs;
        }
        
        for (int i = 0; i < messageNodes.getLength(); i++) {
            Node messageNode = messageNodes.item(i);
            
            if (messageNode.getNodeType() == Node.ELEMENT_NODE) {
                Message msg = parseMessage((Element) messageNode);
                
                if (msg != null) {
                    msgs.add(msg);
                }
            }
        }
        
        return msgs;
    }
    
    public Message parseMessage(Element el) throws XMLMessageParseException {
        Message msg = new Message();
        NodeList children = el.getChildNodes();
        msg.setBody("");
        msg.setSentDate(new Date());
        
        if (children == null || children.getLength() == 0) {
            throw new XMLMessageParseException("Message element cannot be empty");
        }
        
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                String tagName = child.getTagName();
                
                if (tagName.equals("src")) {
                    msg.setSrcAddress(parseAddress(child));
                }
                
                else if (tagName.equals("dest")) {
                    msg.setDestAddress(parseAddress(child));
                }
                
                else if (tagName.equals("body")) {
                    msg.setBody(child.getTextContent());
                }
                
                else if (tagName.equals("sent")) {
                    long sentTime;
                    
                    try {
                        sentTime = Long.parseLong(child.getTextContent());
                    }
                    catch (NumberFormatException e) {
                        throw new XMLMessageParseException("Invalid format for sent date (expected a long integer)", e);
                    }
                    
                    msg.setSentDate(new Date(sentTime));
                }
            }
        }
        
        if (msg.getSrcAddress() == null) {
            throw new XMLMessageParseException("Source address missing or empty");
        }
        
        if (msg.getDestAddress() == null) {
            throw new XMLMessageParseException("Destination address missing or empty");
        }
        
        return msg;
    }
    
    public Address parseAddress(Element el) throws XMLMessageParseException {
        Address addr = new Address();
        NodeList children = el.getChildNodes();
        
        if (children == null || children.getLength() == 0) {
            throw new XMLMessageParseException("Address element cannot be empty");
        }
        
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                String tagName = child.getTagName();
                
                if (tagName.equals("username")) {
                    addr.setUsername(child.getTextContent());
                }
                
                else if (tagName.equals("hostname")) {
                    addr.setHostname(child.getTextContent());
                }
            }
        }
        
        if (addr.getUsername() == null || addr.getUsername().length == 0) {
            throw new XMLMessageParseException("Address username missing or empty");
        }
        
        if (addr.getHostname() == null || addr.getHostname().length == 0) {
            throw new XMLMessageParseException("Address hostname missing or empty");
        }
        
        return addr;
    }
}
