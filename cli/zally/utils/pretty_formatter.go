package utils

import (
	"bytes"
	"fmt"
	"strings"

	"github.com/logrusorgru/aurora"
	"github.com/zalando/zally/cli/zally/domain"
)

// PrettyFormatter is used to generate violations in pretty format
type PrettyFormatter struct {
}

// FormatViolation generates a violation in pretty format
func (f *PrettyFormatter) FormatViolation(violation *domain.Violation) string {
	var buffer bytes.Buffer

	colorize := f.colorizeByTypeFunc(violation.ViolationType)

	fmt.Fprintf(&buffer, "%s %s\n", colorize(violation.ViolationType), colorize(violation.Title))
	fmt.Fprintf(&buffer, "\t%s\n", violation.Decription)
	fmt.Fprintf(&buffer, "\t%s\n", violation.RuleLink)

	for _, path := range violation.Paths {
		fmt.Fprintf(&buffer, "\t\t%s\n", path)
	}

	fmt.Fprintf(&buffer, "\n")

	return buffer.String()
}

// FormatViolationsCount generates violation counters in in pretty format
func (f *PrettyFormatter) FormatViolationsCount(violationsCount *domain.ViolationsCount) string {
	var buffer bytes.Buffer
	fmt.Fprint(&buffer, f.formatHeader("Summary:"))
	fmt.Fprintf(&buffer, "MUST violations: %d\n", violationsCount.Must)
	fmt.Fprintf(&buffer, "SHOULD violations: %d\n", violationsCount.Should)
	fmt.Fprintf(&buffer, "MAY violations: %d\n", violationsCount.May)
	fmt.Fprintf(&buffer, "HINT violations: %d\n", violationsCount.Hint)
	return buffer.String()
}

// FormatRule formats rule description
func (f *PrettyFormatter) FormatRule(rule *domain.Rule) string {
	var buffer bytes.Buffer
	colorize := f.colorizeByTypeFunc(rule.Type)
	fmt.Fprintf(
		&buffer,
		"%s %s: %s\n\t%s\n\n",
		colorize(rule.Code),
		colorize(rule.Type),
		rule.Title,
		rule.URL)
	return buffer.String()
}

func (f *PrettyFormatter) formatHeader(header string) string {
	if len(header) == 0 {
		return ""
	}
	return fmt.Sprintf("%s\n%s\n\n", header, strings.Repeat("=", len(header)))
}

// TODO: move this helper outside of PrettyFormatter
func (f *PrettyFormatter) colorizeByTypeFunc(ruleType string) func(interface{}) aurora.Value {
	switch ruleType {
	case "MUST":
		return aurora.Red
	case "SHOULD":
		return aurora.Brown
	case "MAY":
		return aurora.Green
	case "HINT":
		return aurora.Cyan
	default:
		return aurora.Gray
	}
}
