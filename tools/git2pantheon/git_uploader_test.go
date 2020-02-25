package main

import (
	"bytes"
	"io/ioutil"
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
	uploader := "./pantheon.py"
	getUploader()

	output := captureOutput(func() {
		push2Pantheon("random")
	})
	if !strings.Contains(output, "pantheon2.yml was not found in the root of the repo, skipping upload.") {
		t.Errorf("No pantheon2.yml is expected, this test should skip the upload.")
	}
	cleanup(uploader)
	if _, err := os.Stat(uploader); err == nil {
		t.Errorf("The %s file not should exist", uploader)
	}
}

func TestPush2PantheonWithYML(t *testing.T) {
	uploader := "./pantheon.py"
	yml := "./pantheon2.yml"
	getUploader()

	dummy := []byte("dummy pantheon2.yml")
	err := ioutil.WriteFile("./pantheon2.yml", dummy, 0644)
	if err != nil {
		t.Errorf("No pantheon2.yml was found, and no upload attempted.")
	}

	output := captureOutput(func() {
		push2Pantheon(".")
	})
	if strings.Contains(output, "pantheon2.yml was not found in the root of the repo, skipping upload.") {
		t.Errorf("No pantheon2.yml was found, and no upload attempted.")
	}
	if !strings.Contains(output, "Environment variables not found") {
		t.Errorf("ENV PANTHEON_SERVER, UPLOADER_PASSWORD and UPLOADER_USER should always be not set during build.")
	}
	cleanup(uploader)
	cleanup(yml)

	if _, err := os.Stat(uploader); err == nil {
		t.Errorf("The %s file not should exist", uploader)
	}
	if _, err := os.Stat(yml); err == nil {
		t.Errorf("The %s file not should exist", yml)
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
