package main

import (
	"fmt"
	"log"
	"math/rand"
	"os"
	"os/exec"
	"time"

	"gopkg.in/src-d/go-git.v4"
	"gopkg.in/src-d/go-git.v4/plumbing"
)

func gitClone(repository string, branch string, directory string) {
	log.Printf("git clone -b %s --single-branch %s %s", branch, repository, directory)

	_, err := git.PlainClone(directory, false, &git.CloneOptions{
		URL:           repository,
		ReferenceName: plumbing.ReferenceName(fmt.Sprintf("refs/heads/%s", branch)),
		SingleBranch:  true,
		Progress:      os.Stdout,
	})

	if err == nil {
		//Uploader call still on the same gorutine.
		push2Pantheon(directory)
	} else {
		log.Print(err)
	}

}

func getUploader() {
	if _, err := os.Stat("./pantheon.py"); os.IsNotExist(err) {
		const uploader_url = "https://raw.githubusercontent.com/aprajshekhar/pantheon/ap_request_data_log/uploader/pantheon.py"
		args := []string{"-o", "./pantheon.py", uploader_url}
		cmd := exec.Command("curl", args...)
		out, err := cmd.Output()
		if err != nil {
			//Uploader call still on the same gorutine.
			log.Print(err)
		}
		log.Print("Successfully downloaded the uploader." + string(out))
	}
	log.Print("Setting uploader executable permissions.")
	os.Chmod("./pantheon.py", 0755)
}

func push2Pantheon(directory string) {

	if _, err := os.Stat(directory + "/pantheon2.yml"); os.IsNotExist(err) {
		log.Print("pantheon2.yml was not found in the root of the repo, skipping upload.")
	} else {
		log.Print("Found pantheon2.yml in the root of the repo, uploading.")
		var user = os.Getenv("UPLOADER_USER")
		var password = os.Getenv("UPLOADER_PASSWORD")
		var server = os.Getenv("PANTHEON_SERVER")
		log.Printf("Using user %s, directory %s, and pantheon server %s ", user, directory, server)
		args := []string{"pantheon.py", "push", "--user", user, "--password", password, "--directory", directory, "--server", server}
		if user == "" || password == "" || server == "" {
			log.Print("Environment variables not found, using uploader and pantheon.yml settings.")
			args = []string{"pantheon.py", "push", "--directory", directory}
		}
		//Now call python
		cmd := exec.Command("python3", args...)

		out, err := cmd.Output()

		//Print error if only its not null
		if err != nil {
			log.Print(err)
		}

		log.Print(string(out))
	}

	// Cleanup also happens in the same thread.
	cleanup(directory)
}

func cleanup(directory string) {
	//Todo
	log.Printf("Cleanup: deleting directory: %s", directory)
	os.RemoveAll(directory)
}

func randomAlphaNumericString() string {
	const chars = "0123456789abcdefghijklmnopqrstuvwxyzREDHAT"

	var seed *rand.Rand = rand.New(
		rand.NewSource(time.Now().UnixNano()))

	byteSlice := make([]byte, 10)
	for i := range byteSlice {
		byteSlice[i] = chars[seed.Intn(len(chars))]
	}
	return string(byteSlice)
}
