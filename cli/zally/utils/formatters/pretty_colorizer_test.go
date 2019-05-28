package formatters

import (
	"fmt"
	"testing"

	"github.com/logrusorgru/aurora"
	"github.com/zalando/zally/cli/zally/tests"
)

func TestColorizeByTypeFuncIsUsingColors(t *testing.T) {
	colorizer := NewPrettyColorizer(true)

	t.Run("Returns red when type is MUST", func(t *testing.T) {
		result := colorizer.ColorizeByTypeFunc("MUST")
		tests.AssertEquals(t, aurora.Red("abcde"), result("abcde"))
	})

	t.Run("Returns brown when type is SHOULD", func(t *testing.T) {
		result := colorizer.ColorizeByTypeFunc("SHOULD")
		tests.AssertEquals(t, aurora.Brown("abcde"), result("abcde"))
	})

	t.Run("Returns green when type is MAY", func(t *testing.T) {
		result := colorizer.ColorizeByTypeFunc("MAY")
		tests.AssertEquals(t, aurora.Green("abcde"), result("abcde"))
	})

	t.Run("Returns cyan when type is HINT", func(t *testing.T) {
		result := colorizer.ColorizeByTypeFunc("HINT")
		tests.AssertEquals(t, aurora.Cyan("abcde"), result("abcde"))
	})

	t.Run("Returns white by default", func(t *testing.T) {
		result := colorizer.ColorizeByTypeFunc("WHATEVER")
		tests.AssertEquals(t, aurora.White("abcde"), result("abcde"))
	})
}

func TestColorizeByTypeFuncIsNotUsingColors(t *testing.T) {
	colorizer := NewPrettyColorizer(false)

	t.Run("Returns gray by default", func(t *testing.T) {
		result := colorizer.ColorizeByTypeFunc("MUST")
		tests.AssertEquals(t, "abcde", fmt.Sprint(result("abcde")))
	})
}
