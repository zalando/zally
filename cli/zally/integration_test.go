// +build integration

package main

import (
	"io/ioutil"
	"os"
	"strconv"
	"strings"
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
	out, e := RunAppAndCaptureOutput([]string{""})

	assert.Contains(t, out, "USAGE:")
	assert.Contains(t, out, "zally.test [global options] command [command options] [arguments...]")

	assert.Nil(t, e)
}

func TestIntegrationWithLocalYamlFile(t *testing.T) {
	out, e := RunAppAndCaptureOutput([]string{"", "lint", "../../server/zally-test/src/main/resources/fixtures/api_spa.yaml"})
	assertMoreThanZeroViolations(t, out, e)
}

func TestIntegrationWithLocalJsonFile(t *testing.T) {
	out, e := RunAppAndCaptureOutput([]string{"", "lint", "../../server/zally-test/src/main/resources/fixtures/api_spp.json"})

	assertMoreThanZeroViolations(t, out, e)
}

func TestIntegrationWithRemoteYamlFile(t *testing.T) {
	ts := testHTTPServer()
	defer ts.Close()
	out, e := RunAppAndCaptureOutput([]string{"", "lint", ts.URL + "/api_spa.yaml"})

	assertMoreThanZeroViolations(t, out, e)
}

func TestIntegrationWithRemoteJsonFile(t *testing.T) {
	ts := testHTTPServer()
	defer ts.Close()
	out, e := RunAppAndCaptureOutput([]string{"", "lint", ts.URL + "/api_spp.json"})

	assertMoreThanZeroViolations(t, out, e)
}

func TestIntegrationWithNoMustViolations(t *testing.T) {
	out, e := RunAppAndCaptureOutput([]string{"", "lint", "../../server/zally-test/src/main/resources/fixtures/no_must_violations.yaml"})

	must, should, may, hint := countViolations(out)

	assert.Zero(t, must, "No MUST violations expected")
	assert.Zero(t, should, "No SHOULD violation expected")
	assert.True(t, may > 0, "At least one MAY violation expected")
	assert.Zero(t, hint)
	assert.Nil(t, e)
}

func TestIntegrationDisplayRulesList(t *testing.T) {
	out, e := RunAppAndCaptureOutput([]string{"", "rules"})

	assert.Contains(t, out, "Avoid Link in Header Rule")
	assert.Contains(t, out, "https://zalando.github.io/restful-api-guidelines/#166")

	assert.Nil(t, e)
}

func TestIntegrationNotReceiveDeprecationMessage(t *testing.T) {
	out, e := RunAppAndCaptureOutput([]string{"", "rules"})

	assert.NotContains(t, out, "Please update your CLI:")

	assert.Nil(t, e)
}

func assertMoreThanZeroViolations(t *testing.T, out string, e error) {
	must, should, may, hint := countViolations(out)

	assert.True(t, must > 0)
	assert.True(t, should > 0)
	assert.True(t, may > 0)
	assert.Equal(t, 0, hint)
	assert.NotNil(t, e)
}

func countViolations(out string) (int, int, int, int) {
	must := -1
	should := -1
	may := -1
	hint := -1
	split := strings.Split(out, "\n")
	for _, line := range split {
		must = parseViolations(line, must, "MUST")
		should = parseViolations(line, should, "SHOULD")
		may = parseViolations(line, may, "MAY")
		hint = parseViolations(line, hint, "HINT")
	}
	return must, should, may, hint
}

func parseViolations(line string, oldValue int, severity string) int {
	trim := strings.TrimSpace(line)
	prefix := severity + " violations: "
	if strings.HasPrefix(trim, prefix) {
		i, _ := strconv.Atoi(strings.TrimLeft(trim, prefix))
		return i
	}
	return oldValue
}

func testHTTPServer() *httptest.Server {
	ts := httptest.NewServer(http.HandlerFunc(fileHandler))
	return ts
}

func fileHandler(w http.ResponseWriter, r *http.Request) {
	absolutePath, _ := filepath.Abs("../../server/zally-test/src/main/resources/fixtures/" + r.URL.Path[1:])

	file, err := os.Open(absolutePath)
	if err != nil {
		http.Error(w, "Failed to get file", 500)
	} else {
		io.Copy(w, file)
	}
}
