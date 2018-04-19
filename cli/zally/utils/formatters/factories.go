package formatters

import (
	"fmt"
)

// NewFormatter creates a formatter for a given format
func NewFormatter(format string) (Formatter, error) {

	switch format {
	case "markdown":
		var markdownFormatter MarkdownFormatter
		return &markdownFormatter, nil
	case "pretty":
		colorizer := NewPrettyColorizer(true)
		prettyFormatter := NewPrettyFormatter(colorizer)
		return &prettyFormatter, nil
	case "text":
		colorizer := NewPrettyColorizer(false)
		prettyFormatter := NewPrettyFormatter(colorizer)
		return &prettyFormatter, nil
	default:
		return nil, fmt.Errorf("Please use a supported output format")
	}
}
