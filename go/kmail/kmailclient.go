package main

import (
    "bytes"
    "github.com/kierdavis/argparse"
    "github.com/kierdavis/KMail/go"
    "os"
    "strings"
    "time"
    "io"
    "fmt"
)

type Args struct {
	Address string
	SrcUser string
	SrcHost string
	SendVia string
}

func GetUser() (s string) {
	s = os.Getenv("USERNAME")
	if len(s) > 0 {
		return s
	}
	
	s = os.Getenv("USER")
	if len(s) > 0 {
		return s
	}
	
	return "UNKNOWN"
}

func GetHost() (s string) {
	s = os.Getenv("HOSTNAME")
	if len(s) > 0 {
		return s
	}
	
	return "UNKNOWN"
}

func main() {
	p := argparse.New("KMail client")
	p.Argument("Address", 1, argparse.Store, "ADDRESS", "The destination address.")
	p.Option('U', "user", "SrcUser", 1, argparse.Store, "HOSTNAME", "The source username (defaults to the $HOSTNAME environment variable).")
	p.Option('H', "host", "SrcHost", 1, argparse.Store, "HOSTNAME", "The source hostname (defaults to the $USERNAME or $USER environment variables).")
	p.Option('v', "via", "SendVia", 1, argparse.Store, "HOSTNAME", "The server to send the mail via (defaults to the hostname of the destination address).")
	
	args := new(Args)
	err := p.Parse(args)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Error: %s\n", err.Error())
		return
	}
	
	var src kmail.Address
	var dest kmail.Address
	
	if args.SrcUser != "" {
		src.Username = args.SrcUser
	} else {
		src.Username = GetUser()
	}
	
	if args.SrcHost != "" {
		src.Hostname = args.SrcHost
	} else {
		src.Hostname = GetHost()
	}
	
	pos := strings.Index(args.Address, "@")
	if pos < 0 {
		dest.Username = args.Address
		dest.Hostname = "localhost"
	} else {
		dest.Username = args.Address[:pos]
		dest.Hostname = args.Address[pos+1:]
	}
	
	buffer := new(bytes.Buffer)
	_, err = io.Copy(buffer, os.Stdin)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Error: %s\n", err.Error())
		return
	}
	
	msg := &kmail.Message{
		Src: src,
		Dest: dest,
		Body: string(buffer.Bytes()),
		Sent: time.Now().Unix(),
	}
    
    host := args.SendVia
    if host == "" {
        host = msg.Dest.Hostname
    }
	
	err = kmail.SendMessageTo(msg, host)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Error: %s\n", err.Error())
		return
	}
	
	fmt.Fprintf(os.Stderr, "Message sent.\n")
}
