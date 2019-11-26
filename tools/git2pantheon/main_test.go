package main

import (
	"fmt"
	"io/ioutil"
	"net/http/httptest"
	"strings"
	"testing"
)

func TestCloneBranch(t *testing.T) {
	req := httptest.NewRequest("GET", "http://example.com/clone", nil)
	w := httptest.NewRecorder()
	cloneBranch(w, req)
	resp := w.Result()
	body, _ := ioutil.ReadAll(resp.Body)
	fmt.Println(string(body))

	if !strings.Contains(string(body), "Invalid request method") {
		t.Errorf("This method should only work with a POST. It worked with a GET.")
	}

	req = httptest.NewRequest("POST", "http://example.com/clone", nil)
	w = httptest.NewRecorder()
	cloneBranch(w, req)
	resp = w.Result()
	body, _ = ioutil.ReadAll(resp.Body)
	fmt.Println(string(body))

	if !strings.Contains(string(body), "The repository entered does not look like a git repo.") {
		t.Errorf("The request did not included a valid Repository but the system think it does.")
	}
}

func TestGetInfo(t *testing.T) {
	req := httptest.NewRequest("GET", "http://example.com/info", nil)
	w := httptest.NewRecorder()
	getInfo(w, req)
	resp := w.Result()
	body, _ := ioutil.ReadAll(resp.Body)
	fmt.Println(string(body))

	if !strings.Contains(string(body), "not set") {
		t.Errorf("ENV COMMIT_HASH should always be not set during build.")
	}

	req = httptest.NewRequest("POST", "http://example.com/info", nil)
	w = httptest.NewRecorder()
	getInfo(w, req)
	resp = w.Result()
	body, _ = ioutil.ReadAll(resp.Body)
	fmt.Println(string(body))

	if !strings.Contains(string(body), "Invalid request method") {
		t.Errorf("This method should only work with a GET. It worked with a POST.")
	}
}
