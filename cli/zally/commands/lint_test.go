package commands

import (
	"flag"
	"io/ioutil"
	"testing"
	"time"

	"net/http"
	"net/http/httptest"

	"io"

	"fmt"

	"github.com/urfave/cli"
	"github.com/zalando/zally/cli/zally/domain"
	"github.com/zalando/zally/cli/zally/tests"
	"github.com/zalando/zally/cli/zally/utils"
)

var app = cli.NewApp()

func TestReadFile(t *testing.T) {
	t.Run("fails_if_local_file_is_not_found", func(t *testing.T) {
		data, err := readFile("/tmp/non_existing_file")
		tests.AssertEquals(t, "", data)
		tests.AssertEquals(t, "open /tmp/non_existing_file: no such file or directory", err.Error())
	})

	t.Run("returns_contents_when_local_file_is_found", func(t *testing.T) {
		data, err := readFile("testdata/minimal_swagger.json")
		tests.AssertEquals(t, "{\n  \"swagger\": \"2.0\"\n}\n", data)
		tests.AssertEquals(t, nil, err)
	})

	t.Run("returns_contents_when_file_is_remote", func(t *testing.T) {
		handler := func(w http.ResponseWriter, r *http.Request) {
			fixture, _ := ioutil.ReadFile("testdata/minimal_swagger.json")
			w.Header().Set("Content-Type", "application/json")
			io.WriteString(w, string(fixture))
		}
		testServer := httptest.NewServer(http.HandlerFunc(handler))
		defer testServer.Close()

		data, err := readFile(testServer.URL)
		fmt.Print(testServer.URL)
		tests.AssertEquals(t, "{\n  \"swagger\": \"2.0\"\n}\n", data)
		tests.AssertEquals(t, nil, err)
	})
}

func TestDoRequest(t *testing.T) {
	t.Run("returns_violations_when_success", func(t *testing.T) {
		handler := func(w http.ResponseWriter, r *http.Request) {
			fixture, _ := ioutil.ReadFile("testdata/violations_response.json")
			w.Header().Set("Content-Type", "application/json")
			io.WriteString(w, string(fixture))
		}
		testServer := httptest.NewServer(http.HandlerFunc(handler))
		defer testServer.Close()

		requestBuilder := utils.NewRequestBuilder(testServer.URL, "", app)
		data, _ := readFile("testdata/minimal_swagger.json")

		violations, err := doRequest(requestBuilder, data)

		tests.AssertEquals(t, nil, err)
		tests.AssertEquals(t, "First Violation", violations.Violations[0].Title)
		tests.AssertEquals(t, "Second Violation", violations.Violations[1].Title)
	})

	t.Run("returns_error_if_http_error_occured", func(t *testing.T) {
		handler := func(w http.ResponseWriter, r *http.Request) {
			http.Error(w, "Not Found", 404)
		}
		testServer := httptest.NewServer(http.HandlerFunc(handler))
		defer testServer.Close()

		requestBuilder := utils.NewRequestBuilder(testServer.URL, "", app)
		data, _ := readFile("testdata/minimal_swagger.json")

		violations, err := doRequest(requestBuilder, data)

		tests.AssertEquals(t, "Cannot submit file for linting. HTTP Status: 404, Response: Not Found\n", err.Error())
		tests.AssertEquals(t, (*domain.Violations)(nil), violations)
	})

	t.Run("fails when timeout is reached", func(t *testing.T) {
		handler := func(w http.ResponseWriter, r *http.Request) {
			time.Sleep(6 * time.Second)
			w.Header().Set("Content-Type", "application/json")
			io.WriteString(w, "Hello")
		}
		testServer := httptest.NewServer(http.HandlerFunc(handler))
		defer testServer.Close()

		requestBuilder := utils.NewRequestBuilder(testServer.URL, "", app)
		data, _ := readFile("testdata/minimal_swagger.json")

		violations, err := doRequest(requestBuilder, data)

		expectedError := fmt.Sprintf(
			"Post %s/api-violations: net/http: request canceled"+
				" (Client.Timeout exceeded while awaiting headers)",
			testServer.URL,
		)
		tests.AssertEquals(t, expectedError, err.Error())
		tests.AssertEquals(t, (*domain.Violations)(nil), violations)
	})
}

func TestLint(t *testing.T) {
	t.Run("fails_when_unknown_format_is_specified", func(t *testing.T) {
		err := lint(getLintContext("http://example.com", []string{"testdata/minimal_swagger.json"}, "unknown"))
		tests.AssertEquals(t, "Please use a supported output format", err.Error())
	})

	t.Run("fails_when_no_swagger_file_is_specified", func(t *testing.T) {
		err := lint(getLintContext("http://example.com", []string{}, "markdown"))
		tests.AssertEquals(t, "Please specify Swagger File", err.Error())
	})

	t.Run("succeed_when_swagger_file_is_specified_and_format_is_markdown", func(t *testing.T) {
		testServer := startTestServer("testdata/violations_response_without_must_violations.json")
		defer testServer.Close()

		err := lint(getLintContext(testServer.URL, []string{"testdata/minimal_swagger.json"}, "markdown"))
		tests.AssertEquals(t, nil, err)
	})

	t.Run("returns_no_error_when_no_must_violations", func(t *testing.T) {
		handler := func(w http.ResponseWriter, r *http.Request) {
			fixture, _ := ioutil.ReadFile("testdata/violations_response_without_must_violations.json")
			w.Header().Set("Content-Type", "application/json")
			io.WriteString(w, string(fixture))
		}
		testServer := httptest.NewServer(http.HandlerFunc(handler))
		defer testServer.Close()

		err := lint(getLintContext(testServer.URL, []string{"testdata/minimal_swagger.json"}, "markdown"))

		tests.AssertEquals(t, nil, err)
	})

	t.Run("returns_with_an_error_when_any_must_violations", func(t *testing.T) {
		handler := func(w http.ResponseWriter, r *http.Request) {
			fixture, _ := ioutil.ReadFile("testdata/violations_response.json")
			w.Header().Set("Content-Type", "application/json")
			io.WriteString(w, string(fixture))
		}
		testServer := httptest.NewServer(http.HandlerFunc(handler))
		defer testServer.Close()

		err := lint(getLintContext(testServer.URL, []string{"testdata/minimal_swagger.json"}, "markdown"))

		tests.AssertEquals(t, "\nFailing because: 1 'MUST' violation(s) found\n\n", err.Error())
	})
}

func getLintContext(url string, args []string, format string) *cli.Context {
	globalSet := flag.NewFlagSet("test", 0)
	globalSet.String("linter-service", url, "doc")
	globalSet.String("token", "test-token", "doc")
	globalSet.String("format", format, "doc")

	localSet := flag.NewFlagSet("test", 0)
	localSet.Parse(args)

	globalCtx := cli.NewContext(nil, globalSet, nil)

	return cli.NewContext(cli.NewApp(), localSet, globalCtx)
}

func startTestServer(responseBodyPath string) *httptest.Server {
	handler := func(w http.ResponseWriter, r *http.Request) {
		fixture, _ := ioutil.ReadFile(responseBodyPath)
		w.Header().Set("Content-Type", "application/json")
		io.WriteString(w, string(fixture))
	}
	testServer := httptest.NewServer(http.HandlerFunc(handler))
	return testServer
}
