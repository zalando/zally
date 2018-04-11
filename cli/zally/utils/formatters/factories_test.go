package formatters

import (
	"testing"

	"github.com/zalando/zally/cli/zally/tests"
)

func TestNewFormat(t *testing.T) {

	t.Run("Returns markdown formatter", func(t *testing.T) {
		actualFomatter, err := NewFormatter("markdown")
		var expectedFormatter MarkdownFormatter

		tests.AssertEquals(t, nil, err)
		tests.AssertEquals(t, &expectedFormatter, actualFomatter)
	})

	t.Run("Returns pretty formatter with colors", func(t *testing.T) {
		actualFomatter, err := NewFormatter("pretty")
		expectedFormatter := NewPrettyFormatter(NewPrettyColorizer(true))

		tests.AssertEquals(t, nil, err)
		tests.AssertEquals(t, &expectedFormatter, actualFomatter)
	})

	t.Run("Returns pretty formatter without colors", func(t *testing.T) {
		actualFomatter, err := NewFormatter("text")
		expectedFormatter := NewPrettyFormatter(NewPrettyColorizer(false))

		tests.AssertEquals(t, nil, err)
		tests.AssertEquals(t, &expectedFormatter, actualFomatter)
	})

	t.Run("Returns error when formatter is unknown", func(t *testing.T) {
		actualFomatter, err := NewFormatter("unknown")

		tests.AssertEquals(t, "Please use a supported output format", err.Error())
		tests.AssertEquals(t, nil, actualFomatter)
	})
}
