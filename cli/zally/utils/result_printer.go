package utils

import (
	"fmt"
	"io"
	"strings"

	"github.com/logrusorgru/aurora"
	"github.com/zalando/zally/cli/zally/domain"
)

// ResultPrinter helps to print results to the CLI
type ResultPrinter struct {
	buffer    io.Writer
	formatter Formatter
}

// NewResultPrinter creates an instance of ResultPrinter
func NewResultPrinter(buffer io.Writer, formatter Formatter) ResultPrinter {
	var resultPrinter ResultPrinter
	resultPrinter.buffer = buffer
	resultPrinter.formatter = formatter
	return resultPrinter
}

// PrintRules prints a list of supported rules
func (r *ResultPrinter) PrintRules(rules *domain.Rules) {
	r.printRules(rules.Must())
	r.printRules(rules.Should())
	r.printRules(rules.May())
	r.printRules(rules.Hint())
}

func (r *ResultPrinter) printRules(rules []domain.Rule) {
	for _, rule := range rules {
		r.printRule(&rule)
	}
}

func (r *ResultPrinter) printRule(rule *domain.Rule) {
	colorize := r.colorizeByTypeFunc(rule.Type)
	fmt.Fprintf(
		r.buffer,
		"%s %s: %s\n\t%s\n\n",
		colorize(rule.Code),
		colorize(rule.Type),
		rule.Title,
		rule.URL)
}

// PrintViolations creates string representation of Violation
func (r *ResultPrinter) PrintViolations(v *domain.Violations) {
	r.printViolations("MUST", v.Must())
	r.printViolations("SHOULD", v.Should())
	r.printViolations("MAY", v.May())
	r.printViolations("HINT", v.Hint())

	if len(v.Violations) > 0 {
		fmt.Fprint(r.buffer, r.formatter.FormatViolationsCount(&v.ViolationsCount))
	}
	r.printServerMessage(v.Message)
}

func (r *ResultPrinter) printViolations(header string, violations []domain.Violation) {
	if len(violations) > 0 {
		fmt.Fprint(r.buffer, r.formatHeader(header))
		for _, violation := range violations {
			fmt.Fprint(r.buffer, r.formatter.FormatViolation(&violation))
		}
	}
}

func (r *ResultPrinter) printServerMessage(message string) {
	if message != "" {
		fmt.Fprintf(r.buffer, "\n\n%s%s\n\n\n", r.formatHeader("Server message:"), aurora.Green(message))
	}
}

func (r *ResultPrinter) colorizeByTypeFunc(ruleType string) func(interface{}) aurora.Value {
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

func (r *ResultPrinter) formatHeader(header string) string {
	if len(header) == 0 {
		return ""
	}
	return fmt.Sprintf("%s\n%s\n\n", header, strings.Repeat("=", len(header)))
}
