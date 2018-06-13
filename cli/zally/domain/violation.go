package domain

import (
	"strings"
)

// Violation keeps information about Zally violations
type Violation struct {
	Title         string   `json:"title"`
	Decription    string   `json:"description"`
	ViolationType string   `json:"violation_type"`
	RuleLink      string   `json:"rule_link"`
	Pointer       string   `json:"pointer"`
	Paths         []string `json:"paths"`
}

// ToPointerDisplayString returns the violation's Pointer in user friendly display format
func (v *Violation) ToPointerDisplayString() string {
	display := v.Pointer
	display = strings.TrimPrefix(display, "/")
	display = strings.Replace(display, "/", " > ", -1)
	display = strings.Replace(display, "~1", "/", -1)
	display = strings.Replace(display, "~0", "~", -1)
	return display
}
