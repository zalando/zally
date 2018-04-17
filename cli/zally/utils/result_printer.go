package utils

import (
	"fmt"
	"io"

	"github.com/zalando/zally/cli/zally/domain"
	"github.com/zalando/zally/cli/zally/utils/formatters"
)

// ResultPrinter helps to print results to the CLI
type ResultPrinter struct {
	buffer    io.Writer
	formatter formatters.Formatter
}

// NewResultPrinter creates an instance of ResultPrinter
func NewResultPrinter(buffer io.Writer, formatter formatters.Formatter) ResultPrinter {
	var resultPrinter ResultPrinter
	resultPrinter.buffer = buffer
	resultPrinter.formatter = formatter
	return resultPrinter
}

// PrintViolations creates string representation of Violation
func (r *ResultPrinter) PrintViolations(violations *domain.Violations) {
	if len(violations.Violations) > 0 {
		fmt.Fprint(r.buffer, r.formatter.FormatViolations("MUST", violations.Must()))
		fmt.Fprint(r.buffer, r.formatter.FormatViolations("SHOULD", violations.Should()))
		fmt.Fprint(r.buffer, r.formatter.FormatViolations("MAY", violations.May()))
		fmt.Fprint(r.buffer, r.formatter.FormatViolations("HINT", violations.Hint()))

		fmt.Fprint(r.buffer, r.formatter.FormatViolationsCount(&violations.ViolationsCount))
	}
	fmt.Fprint(r.buffer, r.formatter.FormatServerMessage(violations.Message))
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
