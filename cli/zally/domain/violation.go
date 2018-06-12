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
	return strings.Replace(strings.Replace(strings.Replace(strings.TrimPrefix(v.Pointer, "/"), "/", " > ", -1), "~1", "/", -1), "~0", "~", -1)
}
