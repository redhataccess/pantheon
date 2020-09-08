package main

import (
	"encoding/json"
	"flag"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"os"
	"strings"
)

var (
	// flagPort is the open port the application listens on
	flagPort = flag.String("port", "9666", "Port to listen on")
)

type repository_and_branch struct {
	Repo   string `json:"repo"`
	Branch string `json:"branch"`
}

func cloneBranch(w http.ResponseWriter, r *http.Request) {
	if r.Method == "POST" {
		body, err := ioutil.ReadAll(r.Body)
		w.Header().Set("Access-Control-Allow-Origin", "*")
		if err != nil {
			http.Error(w, "{\"error\" : \"Error reading request body\"} ",
				http.StatusInternalServerError)
		}

		var repo repository_and_branch
		err = json.Unmarshal(body, &repo)

		//check parsing error. If error is not null, log and return 400 if parse failed
		if err != nil {
			log.Printf("Error in unmarshalling request body %s", err)
			//setting the response in JSON format for standardizing error responses
			response := "{\"error\" : \"Error reading request body due to " + err.Error() + " \"}"
			http.Error(w, response, http.StatusBadRequest)
			return
		}

		log.Println("repo:  " + repo.Repo)
		log.Println("branch: " + repo.Branch)

		repository := repo.Repo

		if repository == "" {
			//setting the response in JSON format for standardizing error responses
			http.Error(w, "{\"error\" : \"Error reading repository url\"}", http.StatusInternalServerError)
			// return after setting error response to avoid superfluous error header in logs
			return
		}
		if !strings.Contains(strings.ToUpper(repository), "GIT") {
			//setting the response in JSON format for standardizing error responses
			http.Error(w, "{\"error\" : \"The repository entered does not look like a git repo\"}", http.StatusInternalServerError)
			// return after setting error response to avoid superfluous error header in logs
			return
		}

		branch := repo.Branch
		if branch == "" {
			branch = "master"
		}
		directory := randomAlphaNumericString()

		//start a new goroutine (lightweight thread) to handle clone/push/cleanup
		go gitClone(repository, branch, directory)

		fmt.Fprint(w, "POST done")
	} else {
		log.Println("Invalid request method " + r.Method)
		http.Error(w, "{\"error\" : \"Invalid request method\"}", http.StatusMethodNotAllowed)
	}
}

func getInfo(w http.ResponseWriter, r *http.Request) {
	if r.Method == "GET" {
		w.Header().Set("Access-Control-Allow-Origin", "*")

		var response = os.Getenv("COMMIT_HASH")
		if response == "" {
			response = "not set"
		}
		fmt.Fprint(w, "COMMIT_HASH is : "+response)
	} else {
		http.Error(w, "{\"error\" : \"Invalid request method\"}", http.StatusMethodNotAllowed)
	}
}

func init() {
	log.SetFlags(log.Lmicroseconds | log.Lshortfile)
}

func main() {
	flag.Parse()
	mux := http.NewServeMux()
	mux.HandleFunc("/clone", cloneBranch)
	mux.HandleFunc("/info", getInfo)

	getUploader()

	log.Printf("listening on port %s", *flagPort)
	log.Fatal(http.ListenAndServe(":"+*flagPort, mux))
}
