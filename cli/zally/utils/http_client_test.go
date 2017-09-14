package utils

import (
	"fmt"
	"io"
	"io/ioutil"
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

	"github.com/zalando-incubator/zally/cli/zally/tests"
)

func TestListRules(t *testing.T) {

	t.Run("succeed_when_server_responds_fast", func(t *testing.T) {
		handler := func(w http.ResponseWriter, r *http.Request) {
			w.Header().Set("Content-Type", "application/json")
			io.WriteString(w, "Hello")
		}
		testServer := httptest.NewServer(http.HandlerFunc(handler))
		defer testServer.Close()

		request, err := http.NewRequest("GET", testServer.URL, nil)
		response, err := DoHTTPRequest(request)
		tests.AssertEquals(t, nil, err)

		responseBody, _ := ioutil.ReadAll(response.Body)
		tests.AssertEquals(t, "200 OK", response.Status)
		tests.AssertEquals(t, "Hello", string(responseBody))
	})

	t.Run("fails_when_timeout_is_reached", func(t *testing.T) {
		handler := func(w http.ResponseWriter, r *http.Request) {
			time.Sleep(6 * time.Second)
			w.Header().Set("Content-Type", "application/json")
			io.WriteString(w, "Hello")
		}
		testServer := httptest.NewServer(http.HandlerFunc(handler))
		defer testServer.Close()

		request, err := http.NewRequest("GET", testServer.URL, nil)
		response, err := DoHTTPRequest(request)
		expectedError := fmt.Sprintf(
			"Get %s: net/http: request canceled (Client.Timeout exceeded while awaiting headers)",
			testServer.URL,
		)
		tests.AssertEquals(t, expectedError, err.Error())
		tests.AssertEquals(t, (*http.Response)(nil), response)
	})
}
