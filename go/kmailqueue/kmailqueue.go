package main

import (
	"fmt"
	"github.com/kierdavis/KMail/go"
	"net/http"
	"os"
	"sync"
    "github.com/kierdavis/zlog"
    "strings"
    "time"
)

var Logger = zlog.NewLogger("kmailqueue")

type Queue struct {
	sync.Mutex
	IPAddress string
	Messages []*kmail.Message
}

func (q *Queue) Put(msg *kmail.Message) {
	q.Lock()
	defer q.Unlock()
	
	q.Messages = append(q.Messages, msg)
}

func (q *Queue) Get() (msgs []*kmail.Message) {
	q.Lock()
	defer q.Unlock()
	
	msgs = q.Messages
	q.Messages = nil
	return msgs
}

var Queues = make(map[string]*Queue)
var QueuesLock sync.Mutex

func GetQueue(name string) (queue *Queue, ok bool) {
	QueuesLock.Lock()
	defer QueuesLock.Unlock()
	
	queue, ok = Queues[name]
	return queue, ok
}

func NewQueue(name string, ipaddress string) (queue *Queue) {
	queue = new(Queue)
	queue.IPAddress = ipaddress
	queue.Messages = nil
	
	QueuesLock.Lock()
	defer QueuesLock.Unlock()
	
	Queues[name] = queue
	return queue
}

var Address = kmail.Address{"QUEUE", ""}

func Reply(msg *kmail.Message, body string) {
	Logger.Info("Replying to %s with: %s", msg.Dest.String(), body)
	
	go func() {
		err := msg.Reply(Address, body)
		if err != nil {
			Logger.Error("Error sending reply: %s", err.Error())
		}
	}()
}

func HandleMessage(msg *kmail.Message) {
	hostname := msg.Dest.Hostname
	queue, ok := GetQueue(hostname)
	
	if !ok {
		Reply(msg, fmt.Sprintf("No host named '%s' registered on this queue server.", hostname))
		return
	}
	
	queue.Put(msg)
}

func GetIP(r *http.Request) (ip string) {
	pos := strings.Index(r.RemoteAddr, ":")
	if pos < 0 {
		return r.RemoteAddr
	}
	
	return r.RemoteAddr[:pos]
}

func HandleFetch(w http.ResponseWriter, r *http.Request) {
	hostname := r.FormValue("hostname")
	if hostname == "" {
		Logger.Info("%s %s -> 400 Bad Request: No hostname parameter given.", r.Method, r.RequestURI)
		w.WriteHeader(400)
		fmt.Fprintf(w, "No hostname parameter given.\r\n")
		return
	}
	
	queue, ok := GetQueue(hostname)
	if !ok {
		Logger.Info("%s %s -> 404 Not Found: No host named '%s' registered on this queue server.", r.Method, r.RequestURI, hostname)
		w.WriteHeader(404)
		fmt.Fprintf(w, "No host named '%s' registered on this queue server.\r\n", hostname)
		return
	}
	
	if queue.IPAddress != GetIP(r) {
		Logger.Info("%s %s -> 403 Forbidden: Once created, a queue may only be accessed by the IP address it was created on.", r.Method, r.RequestURI)
		w.WriteHeader(403)
		fmt.Fprintf(w, "Once created, a queue may only be accessed by the IP address it was created on.\r\n")
		return
	}
	
	messages := queue.Get()
	
	Logger.Info("%s %s -> 200 OK: %d messages returned.", r.Method, r.RequestURI, len(messages))
	w.WriteHeader(200)
	
	err := kmail.SerializeXML(w, messages)
	if err != nil {
		Logger.Error("Error serializing messages: %s", err.Error())
		return
	}
}

func HandleRegister(w http.ResponseWriter, r *http.Request) {
	hostname := r.FormValue("hostname")
	if hostname == "" {
		Logger.Info("%s %s -> 400 Bad Request: No hostname parameter given.", r.Method, r.RequestURI)
		w.WriteHeader(400)
		fmt.Fprintf(w, "No hostname parameter given.\r\n")
		return
	}
	
	_, ok := GetQueue(hostname)
	if ok {
		Logger.Info("%s %s -> 400 Bad Request: Hostname '%s' already registered.", r.Method, r.RequestURI, hostname)
		w.WriteHeader(400)
		fmt.Fprintf(w, "Hostname '%s' already registered.\r\n", hostname)
		return
	}
	
	NewQueue(hostname, GetIP(r))
	Logger.Info("%s %s -> 200 OK: Queue '%s' created.", r.Method, r.RequestURI, hostname)
	w.WriteHeader(200)
}

func RunMailServer(bindaddr string) {
	sm := http.NewServeMux()
	sm.HandleFunc("/", kmail.HandleMessages(HandleMessage, Logger.SubSource("kmail")))
	sm.HandleFunc("/fetch", HandleFetch)
	sm.HandleFunc("/register", HandleRegister)
	
	Logger.Info("Listening on %s", bindaddr)
	err := http.ListenAndServe(bindaddr, sm)
	if err != nil {
		Logger.Error("Error running server: %s", err.Error())
		return
	}
}

func main() {
	// Initialise logger
	Logger.AddOutput(zlog.NewConsoleOutput())
	go Logger.DispatchAll()
	
	// Check command line
	if len(os.Args) < 3 {
		fmt.Fprintf(os.Stderr, "Usage: %s <mailhostname> <bindaddr>\n", os.Args[0])
		return
	}
	
	Address.Hostname = os.Args[1]
	
	// Start server
	RunMailServer(os.Args[2])
	
	// Yield to allow logging to complete
	time.Sleep(time.Millisecond)
}
