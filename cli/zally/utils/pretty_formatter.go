package utils

import (
	"bytes"
	"fmt"

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
