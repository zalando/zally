package de.zalando.zally.rule

import de.zalando.zally.core.rulesConfig
import de.zalando.zally.core.CaseChecker
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class CaseCheckerParameterizedTest(private val param: TestParam) {

    class TestParam(val case: String, val term: String, val expectation: Boolean) {
        operator fun not(): TestParam = TestParam(case, term, !expectation)

        override fun toString(): String {
            return "$term ${if (expectation) "matches" else "does not match"} $case"
        }
    }

    companion object {
        val checker = CaseChecker.load(rulesConfig)

        @Parameterized.Parameters(name = "{0}")
        @JvmStatic
        fun parameters(): List<TestParam> {
            val parameters = parameters(
                "camelCase",
                listOf("testCase", "testACRONYMCase", "test1234"),
                listOf("TestCase")
            ) + parameters(
                "PascalCase",
                listOf("TestCase", "ACRONYMTestCase", "TestCase1234"),
                listOf("testCase", "test-case")
            ) + parameters(
                "snake_case",
                listOf("test_case", "test", "v1_id", "test_0_1_2_3"),
                listOf("test__case", "Test_Case", "", "_", "_cust_no", "CUST_NO")
            ) + parameters(
                "SCREAMING_SNAKE_CASE",
                listOf("TEST_CASE", "TEST", "V1_ID", "TEST_0_1_2_3"),
                listOf("TEST__CASE", "TestCase", "_")
            ) + parameters(
                "kebab-case",
                listOf("test-case", "test-case-1234"),
                listOf("test-Case", "testCase")
            ) + parameters(
                "COBOL-CASE",
                listOf("TEST-CASE", "TEST-CASE-1234"),
                listOf("test-case", "testCase")
            ) + parameters(
                "hyphenated-Camel-Case",
                listOf("test-Case", "test-ACRONYMCase", "test1234-C1234ase1234", "test-1234-Case"),
                listOf("Test-Case", "testCase", "TestCase")
            ) + parameters(
                "Hyphenated-Pascal-Case",
                listOf("Test-Case", "X-Flow-Id", "ETag", "Test-Case1234", "Test-Case-1234"),
                listOf("test-Case", "TestCase", "testCase", "1234-Test-Case")
            ) + parameters(
                "Title Case",
                listOf("Test Case", "TestCase", "Test-Case"),
                listOf("test-Case")
            )

            val excess = parameters.map { it.case }.toSet() - checker.cases.keys
            Assertions
                .assertThat(excess)
                .withFailMessage("No such cases: $excess")
                .isEmpty()

            val untested = checker.cases.keys - parameters.map { it.case }
            Assertions
                .assertThat(untested)
                .withFailMessage("Untested cases: $untested")
                .isEmpty()

            val missingMatchTests = checker.cases.keys - parameters.filter { it.expectation }.map { it.case }
            Assertions
                .assertThat(missingMatchTests)
                .withFailMessage("Missing match tests: $missingMatchTests")
                .isEmpty()

            val missingMismatchTests = checker.cases.keys - parameters.filter { !it.expectation }.map { it.case }
            Assertions
                .assertThat(missingMismatchTests)
                .withFailMessage("Missing mismatch tests: $missingMismatchTests")
                .isEmpty()

            return parameters
        }

        private fun parameters(case: String, matches: List<String>, mismatches: List<String>): List<TestParam> {
            return matches.map { TestParam(case, it, true) } +
                mismatches.map { TestParam(case, it, false) } +
                TestParam(case, case, true)
        }
    }

    @Test
    fun test() {
        val regex = checker.cases[param.case]
        val result = regex?.matches(param.term)

        Assertions
            .assertThat(result)
            .withFailMessage("${!param}: ${regex?.pattern}")
            .isEqualTo(param.expectation)
    }
}
