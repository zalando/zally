package utils

import (
	"testing"

	"github.com/logrusorgru/aurora"
	"github.com/zalando/zally/cli/zally/domain"
	"github.com/zalando/zally/cli/zally/tests"
)

func TestFormatViolationInPrettyFormat(t *testing.T) {
	var prettyFormatter PrettyFormatter

	t.Run("Converts violation to string in pretty format", func(t *testing.T) {

		var violation domain.Violation
		violation.Title = "Test Title"
		violation.RuleLink = "http://example.com/violation"
		violation.ViolationType = "MUST"
		violation.Decription = "Test Description"
		violation.Paths = []string{"/path/one", "/path/two"}

		actualResult := prettyFormatter.FormatViolation(&violation)
		expectedResult := "\x1b[31mMUST\x1b[0m \x1b[31mTest Title\x1b[0m\n" +
			"\tTest Description\n" +
			"\thttp://example.com/violation\n" +
			"\t\t/path/one\n" +
			"\t\t/path/two\n\n"

		tests.AssertEquals(t, expectedResult, actualResult)
	})
}

func TestFormatViolationsCount(t *testing.T) {
	var prettyFormatter PrettyFormatter

	t.Run("Converts ViolationsCount to string", func(t *testing.T) {
		var count domain.ViolationsCount
		count.Must = 1
		count.Should = 2
		count.May = 3
		count.Hint = 4

		actualResult := prettyFormatter.FormatViolationsCount(&count)
		expectedResult := "Summary:\n" +
			"========\n\n" +
			"MUST violations: 1\n" +
			"SHOULD violations: 2\n" +
			"MAY violations: 3\n" +
			"HINT violations: 4\n"

		tests.AssertEquals(t, expectedResult, actualResult)
	})
}

func TestFormatRule(t *testing.T) {
	t.Run("Formats single rule", func(t *testing.T) {
		var formatter PrettyFormatter

		var rule domain.Rule
		rule.Title = "Must Rule"
		rule.Type = "MUST"
		rule.Code = "166"
		rule.IsActive = true
		rule.URL = "https://example.com/rule"

		result := formatter.FormatRule(&rule)

		tests.AssertEquals(
			t,
			"\x1b[31m166\x1b[0m \x1b[31mMUST\x1b[0m: Must Rule\n\thttps://example.com/rule\n\n",
			result)
	})
}

func TestColorizeByTypeFunc(t *testing.T) {
	var formatter PrettyFormatter

	t.Run("Returns red when type is MUST", func(t *testing.T) {
		result := formatter.colorizeByTypeFunc("MUST")
		tests.AssertEquals(t, aurora.Red("abcde"), result("abcde"))
	})

	t.Run("Returns brown when type is SHOULD", func(t *testing.T) {
		result := formatter.colorizeByTypeFunc("SHOULD")
		tests.AssertEquals(t, aurora.Brown("abcde"), result("abcde"))
	})

	t.Run("Returns green when type is MAY", func(t *testing.T) {
		result := formatter.colorizeByTypeFunc("MAY")
		tests.AssertEquals(t, aurora.Green("abcde"), result("abcde"))
	})

	t.Run("Returns cyan when type is HINT", func(t *testing.T) {
		result := formatter.colorizeByTypeFunc("HINT")
		tests.AssertEquals(t, aurora.Cyan("abcde"), result("abcde"))
	})

	t.Run("Returns gray by default", func(t *testing.T) {
		result := formatter.colorizeByTypeFunc("WHATEVER")
		tests.AssertEquals(t, aurora.Gray("abcde"), result("abcde"))
	})
}
