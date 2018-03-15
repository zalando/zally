package utils

import "github.com/zalando/zally/cli/zally/domain"

// ViolationFormatter provides an interface for violation formatters
type ViolationFormatter interface {
	Format(violation *domain.Violation) string
}
