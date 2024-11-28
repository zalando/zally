package commands

import (
	"bytes"
	"io"
	"io/ioutil"
	"net/http"
	"net/http/httptest"
	"os"
	"testing"

	"github.com/stretchr/testify/assert"

	"github.com/zalando/zally/cli/zally/utils/formatters"

	"fmt"

	"flag"

	"time"

	"github.com/urfave/cli"
	"github.com/zalando/zally/cli/zally/domain"
	"github.com/zalando/zally/cli/zally/tests"
	"github.com/zalando/zally/cli/zally/utils"
)

func TestListRules(t *testing.T) {

	t.Run("fails_when_wrong_filter_is_specified", func(t *testing.T) {
		handler := func(w http.ResponseWriter, r *http.Request) {
			fixture, _ := ioutil.ReadFile("testdata/rules_response.json")
			w.Header().Set("Content-Type", "application/json")
			io.WriteString(w, string(fixture))
		}
		testServer := httptest.NewServer(http.HandlerFunc(handler))
		defer testServer.Close()

		err := listRules(getSupportedRulesContext(testServer.URL, "mustt", "pretty"))
		tests.AssertEquals(t, "mustt is not supported", err.Error())
	})

	t.Run("fails_when_timeout_is_reached", func(t *testing.T) {
		handler := func(w http.ResponseWriter, r *http.Request) {
			time.Sleep(6 * time.Second)
			w.Header().Set("Content-Type", "application/json")
			io.WriteString(w, "Hello")
		}
		testServer := httptest.NewServer(http.HandlerFunc(handler))
		defer testServer.Close()

		err := listRules(getSupportedRulesContext(testServer.URL, "must", "pretty"))
		expectedError := fmt.Sprintf(
			"Get \"%s/supported-rules?is_active=true&type=must\": context deadline exceeded"+
				" (Client.Timeout exceeded while awaiting headers)",
			testServer.URL,
		)
		assert.Error(t, err, expectedError)
	})

}

func TestFetchRules(t *testing.T) {
	t.Run("returns_rules_list_when_success", func(t *testing.T) {
		handler := func(w http.ResponseWriter, r *http.Request) {
			fixture, _ := ioutil.ReadFile("testdata/rules_response.json")
			w.Header().Set("Content-Type", "application/json")
			io.WriteString(w, string(fixture))
		}
		testServer := httptest.NewServer(http.HandlerFunc(handler))
		defer testServer.Close()

		requestBuilder := utils.NewRequestBuilder(testServer.URL, "Bearer", "", app)
		rules, err := fetchRules(requestBuilder, "", false)

		tests.AssertEquals(t, nil, err)
		tests.AssertEquals(t, len(rules.Rules), 15)
		tests.AssertEquals(t, "166", rules.Rules[0].Code)
		tests.AssertEquals(t, "Avoid Link in Header Rule", rules.Rules[0].Title)
		tests.AssertEquals(t, "https://zalando.github.io/restful-api-guidelines/hyper-media/Hypermedia.html#must-do-not-use-link-headers-with-json-entities", rules.Rules[0].URL)
		tests.AssertEquals(t, true, rules.Rules[0].IsActive)
		tests.AssertEquals(t, "MUST", rules.Rules[0].Type)
	})

	t.Run("returns_error_when_status_is_not_200", func(t *testing.T) {
		handler := func(w http.ResponseWriter, r *http.Request) {
			w.WriteHeader(http.StatusBadRequest)
			io.WriteString(w, "Something went wrong")
		}
		testServer := httptest.NewServer(http.HandlerFunc(handler))
		defer testServer.Close()

		requestBuilder := utils.NewRequestBuilder(testServer.URL, "Bearer", "", app)
		rules, err := fetchRules(requestBuilder, "", false)

		tests.AssertEquals(t, "Cannot submit file for linting. HTTP Status: 400, Response: Something went wrong", err.Error())
		tests.AssertEquals(t, (*domain.Rules)(nil), rules)
	})

	t.Run("supports_type_filter", func(t *testing.T) {
		handler := func(w http.ResponseWriter, r *http.Request) {
			fixture, _ := ioutil.ReadFile("testdata/rules_response.json")
			w.Header().Set("Content-Type", "application/json")
			io.WriteString(w, string(fixture))
			tests.AssertEquals(t, r.URL.RawQuery, "is_active=true&type=must")
		}
		testServer := httptest.NewServer(http.HandlerFunc(handler))
		defer testServer.Close()

		requestBuilder := utils.NewRequestBuilder(testServer.URL, "Bearer", "", app)
		fetchRules(requestBuilder, "must", false)
	})
}

func TestValidateType(t *testing.T) {
	t.Run("returns no error if type is supported", func(t *testing.T) {
		var supportedTypes = []string{"must", "hint", "may", "should", ""}
		for _, supportedType := range supportedTypes {
			tests.AssertEquals(t, nil, validateType(supportedType))
		}
	})

	t.Run("returns error if type is not supported", func(t *testing.T) {
		var unsupportedTypes = []string{"MuSt", "MUST", "something", "mayy"}
		for _, unsupportedType := range unsupportedTypes {
			expectedError := fmt.Sprintf("%s is not supported", unsupportedType)
			tests.AssertEquals(t, expectedError, validateType(unsupportedType).Error())
		}
	})
}

func TestPrintRules(t *testing.T) {
	formatter, _ := formatters.NewFormatter("pretty")

	t.Run("prints rules", func(t *testing.T) {
		var shouldRule domain.Rule
		shouldRule.Title = "Second Rule"
		shouldRule.Type = "SHOULD"
		shouldRule.Code = "S001"
		shouldRule.IsActive = true
		shouldRule.URL = "https://example.com/second-rule"

		var rules domain.Rules
		rules.Rules = []domain.Rule{shouldRule}

		tests.AssertEquals(
			t,
			"\x1b[33mS001\x1b[0m \x1b[33mSHOULD\x1b[0m: Second Rule\n\thttps://example.com/second-rule\n\n",
			capturePrintRulesOutput(&rules, formatter))
	})
}

func getSupportedRulesContext(url string, ruleType string, format string) *cli.Context {
	globalSet := flag.NewFlagSet("test", 0)
	globalSet.String("linter-service", url, "doc")
	globalSet.String("token", "test-token", "doc")
	globalSet.String("format", format, "doc")

	localSet := flag.NewFlagSet("test", 0)
	localSet.String("type", ruleType, "doc")

	globalCtx := cli.NewContext(nil, globalSet, nil)

	return cli.NewContext(cli.NewApp(), localSet, globalCtx)
}

func capturePrintRulesOutput(rules *domain.Rules, formatter formatters.Formatter) string {
	oldStdout := os.Stdout // keep backup of the real stdout
	reader, writer, _ := os.Pipe()
	os.Stdout = writer

	printRules(rules, formatter)

	writer.Close()
	os.Stdout = oldStdout

	var buffer bytes.Buffer
	io.Copy(&buffer, reader)
	return buffer.String()
}
