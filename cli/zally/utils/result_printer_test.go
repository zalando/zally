package utils

import (
	"fmt"
	"testing"

	"bytes"

	"github.com/zalando/zally/cli/zally/domain"
	"github.com/zalando/zally/cli/zally/tests"
	"github.com/zalando/zally/cli/zally/utils/formatters"
)

func TestNewResultPrinter(t *testing.T) {
	t.Run("accepts buffer and result printer", func(t *testing.T) {
		var buffer *bytes.Buffer

		colorizer := formatters.NewPrettyColorizer(true)
		formatter := formatters.NewPrettyFormatter(colorizer)

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

	colorizer := formatters.NewPrettyColorizer(true)
	formatter := formatters.NewPrettyFormatter(colorizer)

	t.Run("Prints sorted rules when found", func(t *testing.T) {
		var buffer bytes.Buffer
		resultPrinter := NewResultPrinter(&buffer, &formatter)

		var rules domain.Rules
		rules.Rules = []domain.Rule{mayRule, shouldRule, mustRule}

		resultPrinter.PrintRules(&rules)

		expectedResult := fmt.Sprintf(
			"%s%s%s",
			formatter.FormatRule(&mustRule),
			formatter.FormatRule(&shouldRule),
			formatter.FormatRule(&mayRule))

		tests.AssertEquals(t, expectedResult, buffer.String())
	})

	t.Run("Prints no rules when not found", func(t *testing.T) {
		var buffer bytes.Buffer
		resultPrinter := NewResultPrinter(&buffer, &formatter)

		var rules domain.Rules
		rules.Rules = []domain.Rule{}

		resultPrinter.PrintRules(&rules)

		tests.AssertEquals(t, "", buffer.String())
	})
}

func TestPrintViolations(t *testing.T) {
	var buffer bytes.Buffer

	colorizer := formatters.NewPrettyColorizer(true)
	formatter := formatters.NewPrettyFormatter(colorizer)
	resultPrinter := NewResultPrinter(&buffer, &formatter)

	var mustViolation domain.Violation
	mustViolation.Title = "Must Title"
	mustViolation.RuleLink = "http://example.com/mustViolation"
	mustViolation.ViolationType = "MUST"
	mustViolation.Decription = "Must Description"
	mustViolation.Paths = []string{"/path/one", "/path/two"}
	mustViolations := []domain.Violation{mustViolation}

	var shouldViolation domain.Violation
	shouldViolation.Title = "Should Title"
	shouldViolation.RuleLink = "http://example.com/shouldViolation"
	shouldViolation.ViolationType = "SHOULD"
	shouldViolation.Decription = "Should Description"
	shouldViolation.Paths = []string{"/path/three", "/path/four"}
	shouldViolations := []domain.Violation{shouldViolation}

	var violationsCount domain.ViolationsCount
	violationsCount.Must = 1
	violationsCount.Should = 2
	violationsCount.May = 3
	violationsCount.Hint = 4

	t.Run("PrintViolations prints nothing if no violations", func(t *testing.T) {
		buffer.Reset()

		var violations domain.Violations
		resultPrinter.PrintViolations(&violations)

		result := string(buffer.Bytes())
		tests.AssertEquals(t, "", result)
	})

	t.Run("PrintViolations returns list of violation strings", func(t *testing.T) {
		buffer.Reset()

		serverMessage := "Hello world!"

		var violations domain.Violations
		violations.Violations = []domain.Violation{mustViolation, shouldViolation}
		violations.ViolationsCount = violationsCount
		violations.Message = serverMessage

		resultPrinter.PrintViolations(&violations)

		actualResult := string(buffer.Bytes())
		expectedResult := fmt.Sprintf(
			"%s%s%s%s",
			formatter.FormatViolations("MUST", mustViolations),
			formatter.FormatViolations("SHOULD", shouldViolations),
			formatter.FormatViolationsCount(&violationsCount),
			formatter.FormatServerMessage(serverMessage))

		tests.AssertEquals(t, expectedResult, actualResult)
	})
}
