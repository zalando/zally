package domain

import (
	"testing"

	"github.com/zalando/zally/cli/zally/tests"
)

func TestViolation(t *testing.T) {
	var violation Violation
	violation.Title = "A Title"
	violation.RuleLink = "http://example.com/"
	violation.ViolationType = "MUST"
	violation.Decription = "Description"

	t.Run("ToPointerDisplayString returns path without leading slash", func(t *testing.T) {
		violation.Pointer = "/pointer"

		actualResult := violation.ToPointerDisplayString()

		expectedResult := "pointer"

		tests.AssertEquals(t, expectedResult, actualResult)
	})

	t.Run("ToPointerDisplayString returns path components separated by >", func(t *testing.T) {
		violation.Pointer = "/pointer/a/b/c"

		actualResult := violation.ToPointerDisplayString()

		expectedResult := "pointer > a > b > c"

		tests.AssertEquals(t, expectedResult, actualResult)
	})

	t.Run("ToPointerDisplayString returns path with ~1 decoded to /", func(t *testing.T) {
		violation.Pointer = "/~1pointer"

		actualResult := violation.ToPointerDisplayString()

		expectedResult := "/pointer"

		tests.AssertEquals(t, expectedResult, actualResult)
	})

	t.Run("ToPointerDisplayString returns path with ~0 decoded to ~", func(t *testing.T) {
		violation.Pointer = "/~0pointer"

		actualResult := violation.ToPointerDisplayString()

		expectedResult := "~pointer"

		tests.AssertEquals(t, expectedResult, actualResult)
	})

	t.Run("ToPointerDisplayString returns path with ~01 decoded to ~0", func(t *testing.T) {
		violation.Pointer = "/~01pointer"

		actualResult := violation.ToPointerDisplayString()

		expectedResult := "~1pointer"

		tests.AssertEquals(t, expectedResult, actualResult)
	})

	t.Run("ToPointerDisplayString returns path with line numbers", func(t *testing.T) {
		violation.Pointer = "/pointer"
		violation.StartLine = 5
		violation.EndLine = 10

		actualResult := violation.ToPointerDisplayString()

		expectedResult := "pointer (lines 5-10)"

		tests.AssertEquals(t, expectedResult, actualResult)
	})

	t.Run("ToPointerDisplayString returns path with single line number", func(t *testing.T) {
		violation.Pointer = "/pointer"
		violation.StartLine = 5
		violation.EndLine = 5

		actualResult := violation.ToPointerDisplayString()

		expectedResult := "pointer (line 5)"

		tests.AssertEquals(t, expectedResult, actualResult)
	})
}
