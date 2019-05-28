package formatters

import "github.com/logrusorgru/aurora"

// PrettyColorizer is used to generate ANSII colors for output
type PrettyColorizer struct {
	auroraInstance aurora.Aurora
}

// NewPrettyColorizer creates an instance of PrettyColorizer
func NewPrettyColorizer(useColors bool) *PrettyColorizer {
	var prettyColorizer PrettyColorizer
	prettyColorizer.auroraInstance = aurora.NewAurora(useColors)
	return &prettyColorizer
}

// ColorizeByTypeFunc returns color function by a given type
func (c *PrettyColorizer) ColorizeByTypeFunc(ruleType string) func(interface{}) aurora.Value {
	switch ruleType {
	case "MUST":
		return c.auroraInstance.Red
	case "SHOULD":
		return c.auroraInstance.Brown
	case "MAY":
		return c.auroraInstance.Green
	case "HINT":
		return c.auroraInstance.Cyan
	default:
		return c.auroraInstance.White
	}
}
