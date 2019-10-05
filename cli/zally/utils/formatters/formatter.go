package formatters

import "github.com/zalando/zally/cli/zally/domain"

// Formatter provides an interface for output formatters
type Formatter interface {
	FormatViolations(header string, violations []domain.Violation) string
	FormatViolationsCount(violationsCount *domain.ViolationsCount) string
	FormatMessage(message string) string
	FormatServerMessage(message string) string
	FormatRule(rule *domain.Rule) string
	FormatErrorMessage(message string) string
}
