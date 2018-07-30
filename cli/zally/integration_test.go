// +build integration

package main

import (
	"fmt"
	"io/ioutil"
	"os"
	"testing"

	"github.com/stretchr/testify/assert"
	"io"
	"net/http"
	"net/http/httptest"
	"path/filepath"
)

type callable func() error

// https://stackoverflow.com/questions/10473800/in-go-how-do-i-capture-stdout-of-a-function-into-a-string
func CaptureOutput(callable callable) (string, error) {
	rescueStdout := os.Stdout
	r, w, _ := os.Pipe()
	os.Stdout = w

	err := callable()

	w.Close()
	outRaw, _ := ioutil.ReadAll(r)
	os.Stdout = rescueStdout

	out := string(outRaw[:])
	return out, err
}

func RunAppAndCaptureOutput(params []string) (string, error) {
	return CaptureOutput(func() error {
		return CreateApp().Run(params)
	})
}

func TestIntegrationWithNoParametersShowsUsage(t *testing.T) {
	t.Run("integrationWithNoParametersShowsUsage", func(t *testing.T) {
		out, e := RunAppAndCaptureOutput([]string{""})

		assert.Contains(t, out, "USAGE:")
		assert.Contains(t, out, "zally.test [global options] command [command options] [arguments...]")

		assert.Nil(t, e)
	})
}

func TestIntegrationWithLocalYamlFile(t *testing.T) {
	t.Run("integrationWithLocalYamlFile", func(t *testing.T) {
		out, e := RunAppAndCaptureOutput([]string{"", "lint", "../../server/src/test/resources/fixtures/api_spa.yaml"})

		assert.Contains(t, out, "MUST violations: 5")
		assert.Contains(t, out, "SHOULD violations: 2")
		assert.Contains(t, out, "MAY violations: 6")
		assert.Contains(t, out, "HINT violations: 0")

		assert.NotNil(t, e)
		assert.Equal(t, "Failing because: 5 must violation(s) found", e.Error())
	})
}

func TestIntegrationWithSomeOtherLocalYamlFile(t *testing.T) {
	t.Run("integrationWithSomeOtherLocalYamlFile", func(t *testing.T) {
		out, e := RunAppAndCaptureOutput([]string{"", "lint", "../../server/src/test/resources/fixtures/api_tinbox.yaml"})

		assert.Contains(t, out, "Provide API Specification using OpenAPI")
		assert.Contains(t, out, "MUST violations: 31")
		assert.Contains(t, out, "SHOULD violations: 25")
		assert.Contains(t, out, "MAY violations: 11")
		assert.Contains(t, out, "HINT violations: 0")

		assert.NotNil(t, e)
		assert.Equal(t, "Failing because: 31 must violation(s) found", e.Error())
	})
}

func TestIntegrationWithLocalJsonFile(t *testing.T) {
	t.Run("integrationWithLocalJsonFile", func(t *testing.T) {
		out, e := RunAppAndCaptureOutput([]string{"", "lint", "../../server/src/test/resources/fixtures/api_spp.json"})

		fmt.Println(out)
		assert.Contains(t, out, "MUST violations: 13")
		assert.Contains(t, out, "SHOULD violations: 13")
		assert.Contains(t, out, "MAY violations: 1")
		assert.Contains(t, out, "HINT violations: 0")

		assert.NotNil(t, e)
		assert.Equal(t, e.Error(), "Failing because: 13 must violation(s) found")
	})
}

func TestIntegrationWithRemoteYamlFile(t *testing.T) {
	t.Run("integrationWithRemoteYamlFile", func(t *testing.T) {
		ts := testHTTPServer()
		defer ts.Close()
		out, e := RunAppAndCaptureOutput([]string{"", "lint", ts.URL + "/api_spa.yaml"})

		assert.Contains(t, out, "MUST violations: 5")
		assert.Contains(t, out, "SHOULD violations: 2")
		assert.Contains(t, out, "MAY violations: 6")
		assert.Contains(t, out, "HINT violations: 0")

		assert.NotNil(t, e)
		assert.Equal(t, "Failing because: 5 must violation(s) found", e.Error())
	})
}

func TestIntegrationWithRemoteJsonFile(t *testing.T) {

	t.Run("integrationWithRemoteJsonFile", func(t *testing.T) {
		ts := testHTTPServer()
		defer ts.Close()
		out, e := RunAppAndCaptureOutput([]string{"", "lint", ts.URL + "/api_spp.json"})

		assert.Contains(t, out, "MUST violations: 13")
		assert.Contains(t, out, "SHOULD violations: 13")
		assert.Contains(t, out, "MAY violations: 1")
		assert.Contains(t, out, "HINT violations: 0")

		assert.NotNil(t, e)
		assert.Equal(t, "Failing because: 13 must violation(s) found", e.Error())
	})
}

func TestIntegrationWithNoMustViolations(t *testing.T) {
	t.Run("integrationWithNoMustViolations", func(t *testing.T) {
		out, e := RunAppAndCaptureOutput([]string{"", "lint", "../../server/src/test/resources/fixtures/no_must_violations.yaml"})

		assert.Contains(t, out, "MUST violations: 0")
		assert.Contains(t, out, "SHOULD violations: 2")
		assert.Contains(t, out, "MAY violations: 1")
		assert.Contains(t, out, "HINT violations: 0")

		assert.Nil(t, e)
	})
}

func TestIntegrationDisplayRulesList(t *testing.T) {
	t.Run("integrationDisplayRulesList", func(t *testing.T) {
		out, e := RunAppAndCaptureOutput([]string{"", "rules"})

		assert.Contains(t, out, "Avoid Link in Header Rule")
		assert.Contains(t, out, "https://zalando.github.io/restful-api-guidelines/#166")

		assert.Nil(t, e)
	})
}

func TestIntegrationNotReceiveDeprecationMessage(t *testing.T) {
	t.Run("notShowDeprecationMessage", func(t *testing.T) {
		out, e := RunAppAndCaptureOutput([]string{"", "rules"})

		assert.NotContains(t, out, "Please update your CLI:")

		assert.Nil(t, e)
	})
}

func testHTTPServer() *httptest.Server {
	ts := httptest.NewServer(http.HandlerFunc(fileHandler))
	return ts
}

func fileHandler(w http.ResponseWriter, r *http.Request) {
	absolutePath, _ := filepath.Abs("../../server/src/test/resources/fixtures/" + r.URL.Path[1:])

	file, err := os.Open(absolutePath)
	if err != nil {
		http.Error(w, "Failed to get file", 500)
	} else {
		io.Copy(w, file)
	}
}
