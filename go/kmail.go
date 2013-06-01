package kmail

import (
    "bytes"
    "encoding/xml"
    "fmt"
    "github.com/kierdavis/zlog"
    "io"
    "net/http"
    "net/url"
    "strings"
    "time"
)

type Address struct {
    Username string `xml:"username"`
    Hostname string `xml:"hostname"`
}

func (addr *Address) String() (s string) {
    return fmt.Sprintf("%s@%s", addr.Username, addr.Hostname)
}

type Message struct {
    XMLName xml.Name `xml:"message"`
    Src Address `xml:"src"`
    Dest Address `xml:"dest"`
    Body string `xml:"body"`
    ReplyVia string `xml:"reply-via"`
    Sent int64 `xml:"sent"`
}

type XMLDocument struct {
    XMLName xml.Name `xml:"messages"`
    Messages []*Message `xml:"message"`
}

func ParseXML(input io.Reader) (msgs []*Message, err error) {
    var doc XMLDocument
    
    err = xml.NewDecoder(input).Decode(&doc)
    if err != nil {
        return nil, err
    }
    
    return doc.Messages, nil
}

func SerializeXML(output io.Writer, msgs []*Message) (err error) {
	var doc XMLDocument
	doc.Messages = msgs
	
	return xml.NewEncoder(output).Encode(doc)
}

type MessageHandler func(*Message)

func HandleMessages(mh MessageHandler, logger *zlog.Logger) (hh http.HandlerFunc) {
	return func(w http.ResponseWriter, r *http.Request) {
		msgs, err := ParseXML(r.Body)
		if err != nil {
			if logger != nil {
				logger.Info("%s %s -> 400 Bad Request: %s", r.Method, r.RequestURI, err.Error())
			}
			
			w.WriteHeader(400)
			fmt.Fprintf(w, "Could not parse request body: %s\r\n", err.Error())
			return
		}
		
		for _, msg := range msgs {
			mh(msg)
		}
		
		if logger != nil {
			logger.Info("%s %s -> 200 OK: %d messages received", r.Method, r.RequestURI, len(msgs))
		}
		
		w.WriteHeader(200)
		fmt.Fprintf(w, "OK\r\n")
	}
}

func SendMessage(msg *Message) (err error) {
    return SendMessageTo(msg, msg.Dest.Hostname)
}

func SendMessageTo(msg *Message, host string) (err error) {
	buffer := new(bytes.Buffer)
	err = SerializeXML(buffer, []*Message{msg})
	if err != nil {
		return err
	}
    
    if strings.Index(host, ":") < 0 {
        host += ":4880"
    }
	
	reqURL := &url.URL{
		Scheme: "http",
		Host: host,
		Path: "/",
	}
	
	req, err := http.NewRequest("POST", reqURL.String(), buffer)
	if err != nil {
		return err
	}
	
	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		return err
	}
	
	if resp.StatusCode != 200 {
		return fmt.Errorf("HTTP request returned %d %s", resp.StatusCode, resp.Status)
	}
	
	return nil
}

func (msg *Message) ComposeReply(sender Address, body string) (reply *Message) {
    return &Message{
        Src: sender,
        Dest: msg.Src,
        Body: body,
        Sent: time.Now().Unix(),
    }
}

func (msg *Message) Reply(sender Address, body string) (err error) {
    reply := msg.ComposeReply(sender, body)
    
    if msg.ReplyVia != "" {
        return SendMessageTo(reply, msg.ReplyVia)
    }
    
    return SendMessage(reply)
}
