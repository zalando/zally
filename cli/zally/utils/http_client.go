package utils

import (
	"crypto/tls"
	"net/http"
	"time"
)

const httpTimeout = 5 * time.Second

// DoHTTPRequest makes an HTTP request with timeout
func DoHTTPRequest(request *http.Request, skipSslVerification bool) (*http.Response, error) {
	timeout := httpTimeout
	var client *http.Client

	if skipSslVerification {
		tr := &http.Transport{
			TLSClientConfig: &tls.Config{InsecureSkipVerify: true},
		}
		client = &http.Client{
			Timeout:   timeout,
			Transport: tr,
		}
	} else {
		client = &http.Client{
			Timeout: timeout,
		}
	}
	return client.Do(request)
}
