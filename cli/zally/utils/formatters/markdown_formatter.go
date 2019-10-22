package formatters

import (
	"bytes"
	"fmt"

	"github.com/zalando/zally/cli/zally/domain"
)

// MarkdownFormatter is used to generate output in markdown format
type MarkdownFormatter struct {
}

// FormatViolations formats the list of the violations
func (f *MarkdownFormatter) FormatViolations(header string, violations []domain.Violation) string {
	var buffer bytes.Buffer
	if len(violations) > 0 {
		fmt.Fprint(&buffer, f.formatHeader(header))
		for _, violation := range violations {
			fmt.Fprint(&buffer, f.formatViolation(&violation))
		}
	}
	return buffer.String()
}

// FormatViolationsCount generates violation counters in in markdown format
func (f *MarkdownFormatter) FormatViolationsCount(violationsCount *domain.ViolationsCount) string {
	var buffer bytes.Buffer
	fmt.Fprint(&buffer, f.formatHeader("Summary:"))
	fmt.Fprintf(&buffer, "- MUST violations: %d\n", violationsCount.Must)
	fmt.Fprintf(&buffer, "- SHOULD violations: %d\n", violationsCount.Should)
	fmt.Fprintf(&buffer, "- MAY violations: %d\n", violationsCount.May)
	fmt.Fprintf(&buffer, "- HINT violations: %d\n", violationsCount.Hint)
	return buffer.String()
}

// FormatRule formats rule description
func (f *MarkdownFormatter) FormatRule(rule *domain.Rule) string {
	var buffer bytes.Buffer
	fmt.Fprintf(
		&buffer,
		"- `[%s %s]` [%s](%s)\n",
		rule.Code,
		rule.Type,
		rule.Title,
		rule.URL)
	return buffer.String()
}

// FormatMessage formats message
func (f *MarkdownFormatter) FormatMessage(message string) string {
	if message != "" {
		return fmt.Sprintf("\n%s\n\n", message)
	}
	return ""
}

// FormatServerMessage formats server message
func (f *MarkdownFormatter) FormatServerMessage(message string) string {
	if message != "" {
		return fmt.Sprintf("\n\n%s%s\n\n\n", f.formatHeader("Server message:"), message)
	}
	return ""
}

func (f *MarkdownFormatter) FormatErrorMessage(message string) string {
	return f.FormatMessage(message)
}

func (f *MarkdownFormatter) formatViolation(violation *domain.Violation) string {
	var buffer bytes.Buffer

	fmt.Fprintf(&buffer, "### `%s` [%s](%s)\n\n", violation.ViolationType, violation.Title, violation.RuleLink)
	fmt.Fprintf(&buffer, "> %s\n\n", violation.Description)

	if violation.Pointer != "" {
		fmt.Fprintf(&buffer, "- %s\n", violation.ToPointerDisplayString())
	} else {
		for _, path := range violation.Paths {
			fmt.Fprintf(&buffer, "- %s\n", path)
		}
	}

	fmt.Fprintf(&buffer, "\n")

	return buffer.String()
}

func (f *MarkdownFormatter) formatHeader(header string) string {
	if len(header) == 0 {
		return ""
	}
	return fmt.Sprintf("## %s\n\n", header)
}
