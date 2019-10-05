package formatters

import (
	"bytes"
	"fmt"
	"strings"

	"github.com/zalando/zally/cli/zally/domain"
)

// PrettyFormatter is used to generate violations in pretty format
type PrettyFormatter struct {
	colorizer *PrettyColorizer
}

// NewPrettyFormatter creates a formatter for text output
func NewPrettyFormatter(colorizer *PrettyColorizer) PrettyFormatter {
	var prettyFormatter PrettyFormatter
	prettyFormatter.colorizer = colorizer
	return prettyFormatter
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

// FormatViolations formats the list of the violations
func (f *PrettyFormatter) FormatViolations(header string, violations []domain.Violation) string {
	var buffer bytes.Buffer
	if len(violations) > 0 {
		fmt.Fprint(&buffer, f.formatHeader(header))
		for _, violation := range violations {
			fmt.Fprint(&buffer, f.formatViolation(&violation))
		}
	}
	return buffer.String()
}

// FormatRule formats rule description
func (f *PrettyFormatter) FormatRule(rule *domain.Rule) string {
	var buffer bytes.Buffer

	colorize := f.colorizer.ColorizeByTypeFunc(rule.Type)
	fmt.Fprintf(
		&buffer,
		"%s %s: %s\n\t%s\n\n",
		colorize(rule.Code),
		colorize(rule.Type),
		rule.Title,
		rule.URL)
	return buffer.String()
}

// FormatMessage formats message
func (f *PrettyFormatter) FormatMessage(message string) string {
	if message != "" {
		return fmt.Sprintf("\n%s\n\n", f.colorizer.auroraInstance.Green(message))
	}
	return ""
}

// FormatServerMessage formats server message
func (f *PrettyFormatter) FormatServerMessage(message string) string {
	if message != "" {
		return fmt.Sprintf("\n\n%s%s\n\n\n", f.formatHeader("Server message:"), f.colorizer.auroraInstance.Green(message))
	}
	return ""
}

func (f *PrettyFormatter) FormatErrorMessage(message string) string {
	if message != "" {
		return fmt.Sprintf("\n%s", f.colorizer.auroraInstance.Red(message))
	}
	return ""
}

func (f *PrettyFormatter) formatHeader(header string) string {
	if len(header) == 0 {
		return ""
	}
	return fmt.Sprintf("%s\n%s\n\n", header, strings.Repeat("=", len(header)))
}

func (f *PrettyFormatter) formatViolation(violation *domain.Violation) string {
	var buffer bytes.Buffer

	colorize := f.colorizer.ColorizeByTypeFunc(violation.ViolationType)

	fmt.Fprintf(&buffer, "%s %s\n", colorize(violation.ViolationType), colorize(violation.Title))
	fmt.Fprintf(&buffer, "\t%s\n", violation.Description)
	fmt.Fprintf(&buffer, "\t%s\n", violation.RuleLink)

	if violation.Pointer != "" {
		fmt.Fprintf(&buffer, "\t\t%s\n", violation.ToPointerDisplayString())
	} else {
		for _, path := range violation.Paths {
			fmt.Fprintf(&buffer, "\t\t%s\n", path)
		}
	}

	fmt.Fprintf(&buffer, "\n")

	return buffer.String()
}
