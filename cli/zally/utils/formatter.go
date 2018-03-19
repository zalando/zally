package utils

import "github.com/zalando/zally/cli/zally/domain"

// Formatter provides an interface for output formatters
type Formatter interface {
	FormatViolation(violation *domain.Violation) string
}
