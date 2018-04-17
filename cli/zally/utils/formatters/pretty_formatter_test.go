package formatters

import (
	"fmt"
	"testing"

	"github.com/zalando/zally/cli/zally/domain"
	"github.com/zalando/zally/cli/zally/tests"
)

func TestPrettyFormatViolations(t *testing.T) {
	prettyFormatter := NewPrettyFormatter(NewPrettyColorizer(true))

	t.Run("FormatViolations returns violations and header", func(t *testing.T) {
		var mustViolation domain.Violation
		mustViolation.Title = "Must Title"
		mustViolation.RuleLink = "http://example.com/mustViolation"
		mustViolation.ViolationType = "MUST"
		mustViolation.Decription = "Must Description"
		mustViolation.Paths = []string{"/path/one", "/path/two"}
		violations := []domain.Violation{mustViolation}

		actualResult := prettyFormatter.FormatViolations("MUST", violations)
		expectedResult := fmt.Sprintf("MUST\n====\n\n%s", prettyFormatter.formatViolation(&mustViolation))

		tests.AssertEquals(t, expectedResult, actualResult)
	})

	t.Run("FormatViolations returns nothing when no violations", func(t *testing.T) {
		result := prettyFormatter.FormatViolations("MUST", []domain.Violation{})
		tests.AssertEquals(t, "", result)
	})

}

func TestPrettyFormatViolationsCount(t *testing.T) {
	prettyFormatter := NewPrettyFormatter(NewPrettyColorizer(true))

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

func TestPrettyFormatRule(t *testing.T) {
	prettyFormatter := NewPrettyFormatter(NewPrettyColorizer(true))

	t.Run("Formats single rule", func(t *testing.T) {
		var rule domain.Rule
		rule.Title = "Must Rule"
		rule.Type = "MUST"
		rule.Code = "166"
		rule.IsActive = true
		rule.URL = "https://example.com/rule"

		result := prettyFormatter.FormatRule(&rule)

		tests.AssertEquals(
			t,
			"\x1b[31m166\x1b[0m \x1b[31mMUST\x1b[0m: Must Rule\n\thttps://example.com/rule\n\n",
			result)
	})
}

func TestPrettyFormatServerMessage(t *testing.T) {
	prettyFormatter := NewPrettyFormatter(NewPrettyColorizer(true))

	t.Run("Formats nothing when no message", func(t *testing.T) {
		actualResult := prettyFormatter.FormatServerMessage("")

		tests.AssertEquals(t, "", actualResult)
	})

	t.Run("Formats message when specified", func(t *testing.T) {
		actualResult := prettyFormatter.FormatServerMessage("Hello world!")
		expectedResult := "\n\nServer message:\n===============\n\n\x1b[32mHello world!\x1b[0m\n\n\n"

		tests.AssertEquals(t, expectedResult, actualResult)
	})
}

func TestPrettyFormatViolationInPrettyFormat(t *testing.T) {
	prettyFormatter := NewPrettyFormatter(NewPrettyColorizer(true))

	t.Run("Converts violation to string in pretty format", func(t *testing.T) {

		var violation domain.Violation
		violation.Title = "Test Title"
		violation.RuleLink = "http://example.com/violation"
		violation.ViolationType = "MUST"
		violation.Decription = "Test Description"
		violation.Paths = []string{"/path/one", "/path/two"}

		actualResult := prettyFormatter.formatViolation(&violation)
		expectedResult := "\x1b[31mMUST\x1b[0m \x1b[31mTest Title\x1b[0m\n" +
			"\tTest Description\n" +
			"\thttp://example.com/violation\n" +
			"\t\t/path/one\n" +
			"\t\t/path/two\n\n"

		tests.AssertEquals(t, expectedResult, actualResult)
	})
}

func TestPrettyFormatHeader(t *testing.T) {
	prettyFormatter := NewPrettyFormatter(NewPrettyColorizer(true))

	t.Run("formatHeader adds a line", func(t *testing.T) {

		actualResult := prettyFormatter.formatHeader("Header")
		expectedResult := "Header\n======\n\n"

		tests.AssertEquals(t, expectedResult, actualResult)
	})

	t.Run("formatHeader returns empty string when no header", func(t *testing.T) {
		result := prettyFormatter.formatHeader("")
		tests.AssertEquals(t, "", result)
	})
}
