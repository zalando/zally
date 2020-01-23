package de.zalando.zally.rule

import de.zalando.zally.apireview.RestApiBaseTest
import de.zalando.zally.core.RulesManager
import de.zalando.zally.util.ErrorResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertTrue
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.test.context.TestPropertySource

@Suppress("UndocumentedPublicClass")
@TestPropertySource(properties = ["zally.ignoreRules=TestCheckAlwaysReport3MustViolations"])
class RestSupportedRulesTest : RestApiBaseTest() {

    private val ignoredRules = listOf("TestCheckAlwaysReport3MustViolations")

    @Autowired
    private lateinit var implementedRules: RulesManager

    @Test
    fun testRulesCount() {
        assertThat(supportedRules.size).isEqualTo(implementedRules.size())
    }

    @Test
    fun testRulesOrdered() {
        val rules = supportedRules
        for (i in 1 until rules.size) {
            val prev = rules[i - 1].type
            val next = rules[i].type
            assertTrue(
                "Item #" + i + " is out of order:\n" +
                    rules.joinToString(separator = "\n") { it.toString() },
                prev!! <= next!!
            )
        }
    }

    @Test
    fun testRulesFields() {
        for ((title, type, url, code) in supportedRules) {
            assertThat(code).isNotEmpty()
            assertThat(title).isNotEmpty()
            assertThat(type).isNotNull
            assertThat(url).isNotNull()
        }
    }

    @Test
    fun testIsActiveFlag() {
        for ((_, _, _, code, active) in supportedRules) {
            assertThat(active).isEqualTo(!ignoredRules.contains(code))
        }
    }

    @Test
    fun testFilterByType() {

        var count = 0
        count += getSupportedRules("MuST", null).size
        count += getSupportedRules("ShOuLd", null).size
        count += getSupportedRules("MaY", null).size
        count += getSupportedRules("HiNt", null).size

        assertThat(count).isEqualTo(implementedRules.size())
    }

    @Test
    fun testReturnsForUnknownType() {
        val response = getSupportedRules("TOPKEK", null, ErrorResponse::class.java)

        assertThat(response.statusCode).isEqualTo(BAD_REQUEST)
        assertThat(response.headers.contentType!!.toString()).isEqualTo(RestApiBaseTest.APPLICATION_PROBLEM_JSON)
        assertThat(response.body!!.title).isEqualTo(BAD_REQUEST.reasonPhrase)
        assertThat(response.body!!.status).isNotEmpty()
        assertThat(response.body!!.detail).isNotEmpty()
    }

    @Test
    fun testFilterByActiveTrue() {
        val rules = getSupportedRules(null, true)
        assertThat(rules.size).isEqualTo(implementedRules.size() - ignoredRules.size)
    }

    @Test
    fun testFilterByActiveFalse() {
        val rules = getSupportedRules(null, false)
        assertThat(rules.size).isEqualTo(ignoredRules.size)
    }
}
