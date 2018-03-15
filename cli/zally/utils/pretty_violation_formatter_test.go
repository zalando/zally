package utils

import (
	"testing"

	"github.com/zalando/zally/cli/zally/domain"
	"github.com/zalando/zally/cli/zally/tests"
)

func TestFormatViolationInPrettyFormat(t *testing.T) {
	var violationFormatter PrettyViolationFormatter

	t.Run("Converts violation to string in pretty format", func(t *testing.T) {

		var violation domain.Violation
		violation.Title = "Test Title"
		violation.RuleLink = "http://example.com/violation"
		violation.ViolationType = "MUST"
		violation.Decription = "Test Description"
		violation.Paths = []string{"/path/one", "/path/two"}

		actualResult := violationFormatter.Format(&violation)
		expectedResult := "\x1b[31mMUST\x1b[0m \x1b[31mTest Title\x1b[0m\n" +
			"\tTest Description\n" +
			"\thttp://example.com/violation\n" +
			"\t\t/path/one\n" +
			"\t\t/path/two\n\n"

		tests.AssertEquals(t, expectedResult, actualResult)
	})
}
