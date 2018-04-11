package formatters

import (
	"fmt"
)

// NewFormatter creates a formatter for a given format
func NewFormatter(format string) (Formatter, error) {

	if format == "markdown" {
		var markdownFormatter MarkdownFormatter
		return &markdownFormatter, nil
	}

	if format == "pretty" {
		colorizer := NewPrettyColorizer(true)
		prettyFormatter := NewPrettyFormatter(colorizer)
		return &prettyFormatter, nil
	}

	if format == "text" {
		colorizer := NewPrettyColorizer(false)
		prettyFormatter := NewPrettyFormatter(colorizer)
		return &prettyFormatter, nil
	}

	return nil, fmt.Errorf("Please use a supported output format")
}
