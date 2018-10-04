package de.zalando.zally.rule

import de.zalando.zally.testConfig
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class CaseCheckerParameterizedTest(val param: TestParam) {

    class TestParam(val case: String, val term: String, val expectation: Boolean) {
        operator fun not(): TestParam = TestParam(case, term, !expectation)

        override fun toString(): String {
            return "$term ${if (expectation) "matches" else "does not match"} $case"
        }
    }

    companion object {
        val checker = CaseChecker.load(testConfig)

        @Parameterized.Parameters(name = "{0}")
        @JvmStatic
        fun parameters(): List<TestParam> {
            val parameters = parameters(
                "Hyphenated-Pascal-Case",
                listOf("Test-Case", "X-Flow-Id", "ETag"),
                listOf("test-Case", "TestCase", "testCase")
            ) + parameters(
                "snake_case",
                listOf("test_case", "test", "v1_id", "0_1_2_3"),
                listOf("test__case", "TestCase", "Test_Case", "", "_", "customer-number", "_customer_number", "CUSTOMER_NUMBER")
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
            return matches.map {
                TestParam(case, it, true)
            } +
                   mismatches.map {
               TestParam(case, it, false)
           }
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