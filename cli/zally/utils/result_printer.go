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
		fmt.Fprint(r.buffer, r.formatter.FormatRule(&rule))
	}
}

// PrintViolations creates string representation of Violation
func (r *ResultPrinter) PrintViolations(v *domain.Violations) {
	if len(v.Violations) > 0 {
		fmt.Fprint(r.buffer, r.formatter.FormatViolations("MUST", v.Must()))
		fmt.Fprint(r.buffer, r.formatter.FormatViolations("SHOULD", v.Should()))
		fmt.Fprint(r.buffer, r.formatter.FormatViolations("MAY", v.May()))
		fmt.Fprint(r.buffer, r.formatter.FormatViolations("HINT", v.Hint()))

		fmt.Fprint(r.buffer, r.formatter.FormatViolationsCount(&v.ViolationsCount))
	}
	r.printServerMessage(v.Message)
}

func (r *ResultPrinter) printServerMessage(message string) {
	if message != "" {
		fmt.Fprintf(r.buffer, "\n\n%s%s\n\n\n", r.formatHeader("Server message:"), aurora.Green(message))
	}
}

func (r *ResultPrinter) formatHeader(header string) string {
	if len(header) == 0 {
		return ""
	}
	return fmt.Sprintf("%s\n%s\n\n", header, strings.Repeat("=", len(header)))
}
