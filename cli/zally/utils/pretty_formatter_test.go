package utils

import (
	"testing"

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
