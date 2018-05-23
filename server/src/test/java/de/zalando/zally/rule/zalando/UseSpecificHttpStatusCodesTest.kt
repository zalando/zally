package de.zalando.zally.rule.zalando

import de.zalando.zally.openApiWithOperations
import de.zalando.zally.rule.Context
import de.zalando.zally.testConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class UseSpecificHttpStatusCodesTest {
    private val codes = UseSpecificHttpStatusCodes(testConfig)

    @Test
    fun shouldPassIfOperationsAreAllowed() {
        val allowedToAll = listOf(
            "200", "301", "400", "401", "403", "404", "405", "406", "408", "410", "428", "429",
            "500", "501", "503", "default"
        )
        val operations = mapOf(
            "get" to listOf("304") + allowedToAll,
            "post" to listOf("201", "202", "207", "303", "415") + allowedToAll,
            "put" to listOf("201", "202", "204", "303", "409", "412", "415", "423") + allowedToAll,
            "patch" to listOf("202", "204", "303", "409", "412", "415", "423") + allowedToAll,
            "delete" to listOf("202", "204", "303", "409", "412", "415", "423") + allowedToAll
        )

        val openApi = openApiWithOperations(operations)
        val context = Context(openApi)

        assertThat(codes.allowOnlySpecificStatusCodes(context)).isEmpty()
    }

    @Test
    fun shouldNotPassIfOperationsAreNotAllowed() {
        val notAllowedAll = listOf(
            "203", "205", "206", "208", "226", "302", "305", "306", "307", "308", "402", "407", "411",
            "413", "414", "416", "417", "418", "421", "422", "424", "426", "431", "451", "502", "504",
            "505", "506", "507", "508", "510", "511"
        )
        val operations = mapOf(
            "get" to listOf("201", "202", "204", "207", "303", "409", "412", "415", "423") + notAllowedAll,
            "post" to listOf("204", "304", "409", "412", "423") + notAllowedAll,
            "put" to listOf("304") + notAllowedAll,
            "patch" to listOf("201", "304") + notAllowedAll,
            "delete" to listOf("201", "304") + notAllowedAll
        )

        val expectedPointers = operations.flatMap { method ->
            method.value.map { code ->
                "#/paths/~1test/${method.key}/responses/$code"
            }
        }

        val openApi = openApiWithOperations(operations)
        val context = Context(openApi)
        val violations = codes.allowOnlySpecificStatusCodes(context)

        assertThat(violations).isNotEmpty
        assertThat(violations.map { it.pointer }).containsExactlyInAnyOrder(*expectedPointers.toTypedArray())
    }
}
