package formatters

import (
	"fmt"
	"testing"

	"github.com/zalando/zally/cli/zally/domain"
	"github.com/zalando/zally/cli/zally/tests"
)

func TestMarkdownFormatViolations(t *testing.T) {

	var markdownFormatter MarkdownFormatter

	t.Run("Returns empty string then no violations", func(t *testing.T) {
		var violations = []domain.Violation{}

		header := "MUST Violations"
		actualResult := markdownFormatter.FormatViolations(header, violations)
		tests.AssertEquals(t, "", actualResult)
	})

	t.Run("Formats list of violations", func(t *testing.T) {
		var firstViolation domain.Violation
		firstViolation.Title = "Test Must"
		firstViolation.RuleLink = "http://example.com/first-violation"
		firstViolation.ViolationType = "MUST"
		firstViolation.Description = "Test Description"
		firstViolation.Paths = []string{"/path/one", "/path/two"}

		var secondViolation domain.Violation
		secondViolation.Title = "Test Should"
		secondViolation.RuleLink = "http://example.com/second-violation"
		secondViolation.ViolationType = "SHOULD"
		secondViolation.Description = "Test Description"
		secondViolation.Paths = []string{"/path/one", "/path/two"}

		var violations = []domain.Violation{firstViolation, secondViolation}

		header := "MUST Violations"
		actualResult := markdownFormatter.FormatViolations(header, violations)
		expectedResult := fmt.Sprintf(
			"%s%s%s",
			markdownFormatter.formatHeader(header),
			markdownFormatter.formatViolation(&firstViolation),
			markdownFormatter.formatViolation(&secondViolation))
		tests.AssertEquals(t, expectedResult, actualResult)
	})
}

func TestMarkdownFormatViolationsCount(t *testing.T) {
	var markdownFormatter MarkdownFormatter

	t.Run("Converts ViolationsCount to string", func(t *testing.T) {
		var count domain.ViolationsCount
		count.Must = 1
		count.Should = 2
		count.May = 3
		count.Hint = 4

		actualResult := markdownFormatter.FormatViolationsCount(&count)
		expectedResult := "## Summary:\n\n" +
			"- MUST violations: 1\n" +
			"- SHOULD violations: 2\n" +
			"- MAY violations: 3\n" +
			"- HINT violations: 4\n"

		tests.AssertEquals(t, expectedResult, actualResult)
	})
}

func TestMarkdownFormatRule(t *testing.T) {
	t.Run("Formats single rule", func(t *testing.T) {
		var formatter MarkdownFormatter

		var rule domain.Rule
		rule.Title = "Must Rule"
		rule.Type = "MUST"
		rule.Code = "166"
		rule.IsActive = true
		rule.URL = "https://example.com/rule"

		result := formatter.FormatRule(&rule)

		tests.AssertEquals(
			t,
			"- `[166 MUST]` [Must Rule](https://example.com/rule)\n",
			result)
	})
}

func TestMarkdownFormatMessage(t *testing.T) {
	var formatter MarkdownFormatter

	t.Run("Formats nothing when no message", func(t *testing.T) {
		actualResult := formatter.FormatMessage("")

		tests.AssertEquals(t, "", actualResult)
	})

	t.Run("Formats message when specified", func(t *testing.T) {
		actualResult := formatter.FormatMessage("Congratulations!")
		expectedResult := "\nCongratulations!\n\n"

		tests.AssertEquals(t, expectedResult, actualResult)
	})
}

func TestMarkdownFormatServerMessage(t *testing.T) {
	var formatter MarkdownFormatter

	t.Run("Formats nothing when no message", func(t *testing.T) {
		actualResult := formatter.FormatServerMessage("")

		tests.AssertEquals(t, "", actualResult)
	})

	t.Run("Formats message when specified", func(t *testing.T) {
		actualResult := formatter.FormatServerMessage("Hello world!")
		expectedResult := "\n\n## Server message:\n\nHello world!\n\n\n"

		tests.AssertEquals(t, expectedResult, actualResult)
	})
}

func TestMarkdownFormatViolation(t *testing.T) {
	var markdownFormatter MarkdownFormatter

	var violation domain.Violation
	violation.Title = "Test Title"
	violation.RuleLink = "http://example.com/violation"
	violation.ViolationType = "MUST"
	violation.Description = "Test Description"

	t.Run("Converts violation to string in pretty format with just paths", func(t *testing.T) {

		violation.Paths = []string{"/path/one", "/path/two"}

		actualResult := markdownFormatter.formatViolation(&violation)
		expectedResult := "### `MUST` [Test Title](http://example.com/violation)\n\n" +
			"> Test Description\n\n" +
			"- /path/one\n" +
			"- /path/two\n\n"

		tests.AssertEquals(t, expectedResult, actualResult)
	})

	t.Run("Converts violation to string in pretty format with paths and pointer", func(t *testing.T) {

		violation.Pointer = "/paths/~1~01things/get/responses/200/content/*~1*"

		actualResult := markdownFormatter.formatViolation(&violation)
		expectedResult := "### `MUST` [Test Title](http://example.com/violation)\n\n" +
			"> Test Description\n\n" +
			"- paths > /~1things > get > responses > 200 > content > */*\n\n"

		tests.AssertEquals(t, expectedResult, actualResult)
	})

	t.Run("Converts violation to string in pretty format with just pointer", func(t *testing.T) {

		violation.Paths = nil

		actualResult := markdownFormatter.formatViolation(&violation)
		expectedResult := "### `MUST` [Test Title](http://example.com/violation)\n\n" +
			"> Test Description\n\n" +
			"- paths > /~1things > get > responses > 200 > content > */*\n\n"

		tests.AssertEquals(t, expectedResult, actualResult)
	})

}

func TestMarkdownFormatHeader(t *testing.T) {
	var formatter MarkdownFormatter

	t.Run("formatHeader adds a line", func(t *testing.T) {

		actualResult := formatter.formatHeader("Header")
		expectedResult := "## Header\n\n"

		tests.AssertEquals(t, expectedResult, actualResult)
	})

	t.Run("formatHeader returns empty string when no header", func(t *testing.T) {
		result := formatter.formatHeader("")
		tests.AssertEquals(t, "", result)
	})
}

func TestMarkdownFormatErrorMessage(t *testing.T) {
	var formatter MarkdownFormatter
	t.Run("Formats nothing when no message", func(t *testing.T) {
		answer := formatter.FormatErrorMessage("")
		tests.AssertEquals(t, "", answer)
	})

	t.Run("Formats error message when specified", func(t *testing.T) {
		actualResult := formatter.FormatErrorMessage("Error")
		expectedResult := "\nError\n\n"

		tests.AssertEquals(t, expectedResult, actualResult)
	})

}
