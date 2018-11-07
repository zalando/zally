package domain

import (
	"fmt"
	"strings"
)

// Violation keeps information about Zally violations
type Violation struct {
	Title         string   `json:"title"`
	Decription    string   `json:"description"`
	ViolationType string   `json:"violation_type"`
	RuleLink      string   `json:"rule_link"`
	Pointer       string   `json:"pointer"`
	StartLine     int      `json:"start_line"`
	EndLine       int      `json:"end_line"`
	Paths         []string `json:"paths"`
}

// ToPointerDisplayString returns the pointer of the violation in user friendly display format
func (v *Violation) ToPointerDisplayString() string {
	display := v.Pointer
	display = strings.TrimPrefix(display, "/")
	display = strings.Replace(display, "/", " > ", -1)
	display = strings.Replace(display, "~1", "/", -1)
	display = strings.Replace(display, "~0", "~", -1)

	if v.StartLine == 0 || v.EndLine == 0 {
		return display
	} else if v.StartLine == v.EndLine {
		return fmt.Sprintf("%s (line %d)", display, v.StartLine)
	} else {
		return fmt.Sprintf("%s (lines %d-%d)", display, v.StartLine, v.EndLine)
	}
}
