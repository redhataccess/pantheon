package main

import (
	"bytes"
	"log"
	"os"
	"strings"
	"testing"
)

func TestRandomAlphaNumericString(t *testing.T) {
	got := randomAlphaNumericString()
	length := len(got)
	if length != 10 {
		t.Errorf("randomAlphaNumericString() = %s; want a length of 10", got)
	}
}

func TestCleanup(t *testing.T) {
	dirName := "./golangTestDir"
	os.Mkdir(dirName, 0777)
	if _, err := os.Stat(dirName); os.IsNotExist(err) {
		t.Errorf("Directory %s should exist", dirName)
	}
	cleanup(dirName)
	if _, err := os.Stat(dirName); err == nil {
		t.Errorf("Directory %s not should exist", dirName)
	}
}

func TestGetUploader(t *testing.T) {
	file := "./pantheon.py"
	getUploader()
	if _, err := os.Stat(file); os.IsNotExist(err) {
		t.Errorf("The %s file should exist", file)
	}
	cleanup(file)
	if _, err := os.Stat(file); err == nil {
		t.Errorf("The %s file not should exist", file)
	}
}

func TestPush2Pantheon(t *testing.T) {
	file := "./pantheon.py"
	getUploader()

	output := captureOutput(func() {
		push2Pantheon("random")
	})
	if !strings.Contains(output, "pantheon2.yml was not found in the root of the repo, skipping upload.") {
		t.Errorf("No pantheon2.yml is expected, this test should skip the upload.")
	}
	cleanup(file)
	if _, err := os.Stat(file); err == nil {
		t.Errorf("The %s file not should exist", file)
	}
}

func TestGitClone(t *testing.T) {
	dirName := "./golangTestDir"
	output := captureOutput(func() {
		gitClone("repo", "branch", dirName)
	})
	if !strings.Contains(output, "repository not found") {
		t.Errorf("No repo should be downloaded for this test")
	}
}

func captureOutput(f func()) string {
	var buf bytes.Buffer
	log.SetOutput(&buf)
	f()
	log.SetOutput(os.Stderr)
	return buf.String()
}
