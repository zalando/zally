package utils

import (
	"fmt"
	"github.com/stretchr/testify/assert"
	"io"
	"io/ioutil"
	"net/http"
	"net/http/httptest"
	"testing"
	"time"
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
		assert.Nil(t, err)

		responseBody, _ := ioutil.ReadAll(response.Body)
		assert.Equal(t, "200 OK", response.Status)
		assert.Equal(t, "Hello", string(responseBody))
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
			"Get \"%s\": context deadline exceeded (Client.Timeout exceeded while awaiting headers)",
			testServer.URL,
		)
		assert.EqualErrorf(t, err, expectedError, "Unexpected error returned")
		assert.Nil(t, response)
	})
}
