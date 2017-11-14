package de.zalando.zally.rule.zalando

import de.zalando.zally.swaggerWithOperations
import de.zalando.zally.testConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NotSpecifyStandardErrorCodesRuleTest {
    private val allHttpStatusCodes = setOf(
            "200", "301", "400", "401", "403", "404", "405", "406", "408", "410", "428", "429",
            "500", "501", "503", "304", "201", "202", "204", "303", "409", "412", "415", "423"
    )
    private val standardErrorCodes = setOf("401", "403", "404", "405", "406", "408", "413", "414", "415", "500", "502", "503", "504")
    private val notStandardErrorCodes = allHttpStatusCodes - standardErrorCodes

    private val allOperations = listOf("get", "post", "put", "patch", "delete", "head", "options")

    private val rule = NotSpecifyStandardErrorCodesRule(ZalandoRuleSet(), testConfig)

    @Test
    fun shouldPassIfErrorCodeNotStandard() {
        val operations = allOperations.associateBy({ it }, { notStandardErrorCodes })

        val swagger = swaggerWithOperations(operations)

        assertThat(rule.validate(swagger)).isNull()
    }

    @Test
    fun shouldNotPassIfErrorCodeIsStandard() {
        val notStandardErrorCodeOperations = allOperations.associateBy({ it }, { notStandardErrorCodes })
        val standardErrorCodeOperations = allOperations.associateBy({ it }, { standardErrorCodes })
        val operations = notStandardErrorCodeOperations + standardErrorCodeOperations

        val swagger = swaggerWithOperations(operations)

        val expectedPaths = standardErrorCodeOperations.flatMap { method ->
            method.value.map {
                code ->
                "/test ${method.key.toUpperCase()} $code"
            }
        }

        val violation = rule.validate(swagger)

        assertThat(violation?.paths.orEmpty()).containsExactlyInAnyOrder(*expectedPaths.toTypedArray())
    }
}
