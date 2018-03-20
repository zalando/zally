package utils

import (
	"fmt"
	"testing"

	"bytes"

	"github.com/zalando/zally/cli/zally/domain"
	"github.com/zalando/zally/cli/zally/tests"
)

func TestNewResultPrinter(t *testing.T) {
	t.Run("accepts buffer and result printer", func(t *testing.T) {
		var buffer *bytes.Buffer
		var formatter PrettyFormatter

		resultPrinter := NewResultPrinter(buffer, &formatter)

		tests.AssertEquals(t, buffer, resultPrinter.buffer)
		tests.AssertEquals(t, &formatter, resultPrinter.formatter)
	})
}

func TestPrintRules(t *testing.T) {
	var mustRule domain.Rule
	mustRule.Title = "First Rule"
	mustRule.Type = "MUST"
	mustRule.Code = "166"
	mustRule.IsActive = true
	mustRule.URL = "https://example.com/first-rule"

	var shouldRule domain.Rule
	shouldRule.Title = "Second Rule"
	shouldRule.Type = "SHOULD"
	shouldRule.Code = "S001"
	shouldRule.IsActive = true
	shouldRule.URL = "https://example.com/second-rule"

	var mayRule domain.Rule
	mayRule.Title = "Third Rule"
	mayRule.Type = "MAY"
	mayRule.Code = "C001"
	mayRule.IsActive = true
	mayRule.URL = "https://example.com/third-rule"

	t.Run("Prints sorted rules when found", func(t *testing.T) {
		var buffer bytes.Buffer
		var formatter PrettyFormatter
		resultPrinter := NewResultPrinter(&buffer, &formatter)

		var rules domain.Rules
		rules.Rules = []domain.Rule{mayRule, shouldRule, mustRule}

		resultPrinter.PrintRules(&rules)

		tests.AssertEquals(
			t,
			"\x1b[31m166\x1b[0m \x1b[31mMUST\x1b[0m: First Rule\n\thttps://example.com/first-rule\n\n\x1b[33mS001\x1b[0m "+
				"\x1b[33mSHOULD\x1b[0m: Second Rule\n\thttps://example.com/second-rule\n\n\x1b[32mC001\x1b[0m "+
				"\x1b[32mMAY\x1b[0m: Third Rule\n\thttps://example.com/third-rule\n\n",
			buffer.String())
	})

	t.Run("Prints no rules when not found", func(t *testing.T) {
		var buffer bytes.Buffer
		var formatter PrettyFormatter
		resultPrinter := NewResultPrinter(&buffer, &formatter)

		var rules domain.Rules
		rules.Rules = []domain.Rule{}

		resultPrinter.PrintRules(&rules)

		tests.AssertEquals(t, "", buffer.String())
	})
}

func TestFormatHeader(t *testing.T) {
	var buffer bytes.Buffer
	var formatter PrettyFormatter
	resultPrinter := NewResultPrinter(&buffer, &formatter)

	t.Run("formatHeader adds a line", func(t *testing.T) {

		actualResult := resultPrinter.formatHeader("Header")
		expectedResult := "Header\n======\n\n"

		tests.AssertEquals(t, expectedResult, actualResult)
	})

	t.Run("formatHeader returns empty string when no header", func(t *testing.T) {
		result := resultPrinter.formatHeader("")
		tests.AssertEquals(t, "", result)
	})
}

func TestPrintViolations(t *testing.T) {
	var buffer bytes.Buffer
	var formatter PrettyFormatter
	resultPrinter := NewResultPrinter(&buffer, &formatter)

	var mustViolation domain.Violation
	mustViolation.Title = "Must Title"
	mustViolation.RuleLink = "http://example.com/mustViolation"
	mustViolation.ViolationType = "MUST"
	mustViolation.Decription = "Must Description"
	mustViolation.Paths = []string{"/path/one", "/path/two"}

	var shouldViolation domain.Violation
	shouldViolation.Title = "Should Title"
	shouldViolation.RuleLink = "http://example.com/shouldViolation"
	shouldViolation.ViolationType = "SHOULD"
	shouldViolation.Decription = "Should Description"
	shouldViolation.Paths = []string{"/path/three", "/path/four"}

	var violationsCount domain.ViolationsCount
	violationsCount.Must = 1
	violationsCount.Should = 2
	violationsCount.May = 3
	violationsCount.Hint = 4

	var violations domain.Violations
	violations.Violations = []domain.Violation{mustViolation, shouldViolation}
	violations.ViolationsCount = violationsCount
	violations.Message = "Hello world!"

	t.Run("printViolations prints violations and header", func(t *testing.T) {
		buffer.Reset()
		resultPrinter.printViolations("MUST", violations.Must())

		actualResult := string(buffer.Bytes())
		expectedResult := fmt.Sprintf("MUST\n====\n\n%s", formatter.FormatViolation(&mustViolation))

		tests.AssertEquals(t, expectedResult, actualResult)
	})

	t.Run("printViolations prints nothing when no violations", func(t *testing.T) {
		buffer.Reset()
		resultPrinter.printViolations("MUST", []domain.Violation{})

		result := string(buffer.Bytes())

		tests.AssertEquals(t, "", result)
	})

	t.Run("PrintViolations prints nothing if no violations", func(t *testing.T) {
		buffer.Reset()

		var violations domain.Violations
		resultPrinter.PrintViolations(&violations)

		result := string(buffer.Bytes())
		tests.AssertEquals(t, "", result)
	})

	t.Run("PrintViolations returns list of violation strings", func(t *testing.T) {
		buffer.Reset()

		resultPrinter.PrintViolations(&violations)

		actualResult := string(buffer.Bytes())
		expectedResult := fmt.Sprintf(
			"MUST\n====\n\n%sSHOULD\n======\n\n%s%s\n\n"+
				"Server message:\n===============\n\n\x1b[32mHello world!\x1b[0m\n\n\n",
			formatter.FormatViolation(&mustViolation),
			formatter.FormatViolation(&shouldViolation),
			formatter.FormatViolationsCount(&violationsCount))

		tests.AssertEquals(t, expectedResult, actualResult)
	})
}

func TestPrintServerMessage(t *testing.T) {
	t.Run("Prints nothing when no message", func(t *testing.T) {
		var buffer bytes.Buffer
		var formatter PrettyFormatter
		resultPrinter := NewResultPrinter(&buffer, &formatter)

		resultPrinter.printServerMessage("")

		actualResult := string(buffer.Bytes())
		tests.AssertEquals(t, "", actualResult)
	})

	t.Run("Prints message when specified", func(t *testing.T) {
		var buffer bytes.Buffer
		var formatter PrettyFormatter
		resultPrinter := NewResultPrinter(&buffer, &formatter)

		resultPrinter.printServerMessage("Hello world!")

		actualResult := string(buffer.Bytes())
		expectedResult := "\n\nServer message:\n===============\n\n\x1b[32mHello world!\x1b[0m\n\n\n"
		tests.AssertEquals(t, expectedResult, actualResult)
	})
}
