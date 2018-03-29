package utils

import "github.com/logrusorgru/aurora"

// PrettyColorizer is used to generate ANSII colors for output
type PrettyColorizer struct {
}

// ColorizeByTypeFunc returns color function by a given type
func (f *PrettyColorizer) ColorizeByTypeFunc(ruleType string) func(interface{}) aurora.Value {
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
