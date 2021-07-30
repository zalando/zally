package org.zalando.zally.maven.plugin

import com.fasterxml.jackson.core.JsonPointer
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.logging.Log
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.Spy
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.refEq
import org.mockito.kotlin.whenever
import org.zalando.zally.core.ApiValidator
import org.zalando.zally.core.RulesPolicy
import org.zalando.zally.rule.api.Severity
import java.io.File
import java.net.URI
import java.util.Collections.singletonList

class ZallyMojoTest() {

    private val log: TestLog = TestLog()

    private val violationOfNonmandatoryRule = org.zalando.zally.core.Result(
        "147",
        URI.create("https://opensource.zalando.com/restful-api-guidelines/#147"),
        "Limit number of Sub-resources level",
        "Number of sub-resources should not exceed 3",
        Severity.SHOULD,
        JsonPointer.compile("/zxc/vbn"),
        null
    )
    private val violationOfMandatoryRuleOne = org.zalando.zally.core.Result(
        "129",
        URI.create("https://opensource.zalando.com/restful-api-guidelines/#129"),
        "Lowercase words with hyphens",
        "Use lowercase separate words with hyphens for path segments",
        Severity.MUST,
        JsonPointer.compile("/qaz/wsx"),
        null
    )
    private val violationOfMandatoryRuleTwo = org.zalando.zally.core.Result(
        "136",
        URI.create("https://opensource.zalando.com/restful-api-guidelines/#136"),
        "Avoid Trailing Slashes",
        "Rule avoid trailing slashes is not followed",
        Severity.MUST,
        JsonPointer.compile("/qwe/rty"),
        null
    )

    private val inputDir = "/specs"
    private val ignoredRules = listOf("123456")
    private val rulesPolicy = RulesPolicy(ignoredRules)

    @Spy
    private lateinit var zallyMojo: ZallyMojo
    @Mock
    private lateinit var apiValidator: ApiValidator

    init {
        MockitoAnnotations.openMocks(this)
        zallyMojo.inputDir = inputDir
        zallyMojo.ignoredRules = ignoredRules
        doReturn(log).whenever(zallyMojo).log
        doReturn(apiValidator).whenever(zallyMojo).createApiValidator(any())
        doReturn(File(this.javaClass.getResource(inputDir)!!.toURI()).walk().filter { it.isFile })
            .whenever(zallyMojo).getFiles(inputDir)
    }

    @AfterEach
    fun clearLog() {
        log.result.clear()
    }

    private val expectedLogOne = """
        \[info] 
        \[info] Path: .+1\\.txt
        \[warn] Spec breaks the rules:
        \[warn] {3}Violations of a rule ${violationOfNonmandatoryRule.id} (see ${violationOfNonmandatoryRule.url}) :
        \[warn] {5}- pointer: ${violationOfNonmandatoryRule.pointer}
        \[warn] {7}description: ${violationOfNonmandatoryRule.description}
        \[error] {3}Violations of a rule ${violationOfMandatoryRuleOne.id} (see ${violationOfMandatoryRuleOne.url}) :
        \[error] {5} - pointer: ${violationOfMandatoryRuleOne.pointer}
        \[error] {7}description: ${violationOfMandatoryRuleOne.description}
        \[error] {3}Violations of a rule ${violationOfMandatoryRuleTwo.id} (see ${violationOfMandatoryRuleTwo.url}) :
        \[error] {5}- pointer: ${violationOfMandatoryRuleTwo.pointer}
        \[error] {7}description: ${violationOfMandatoryRuleTwo.description}
        \[info] 
        \[info] Path: .+2\\.txt
        \[info] Spec follows the rules
        \[info] 
        \[info] Overall results:
        \[info] Violations of nonmandatory rules: 1
        \[info] Violations of mandatory rules: 2
        
    """.trimIndent().toRegex()

    @Test
    fun `should throw exception, because the first spec has violations of mandatory (MUST) rules`() {
        whenever(apiValidator.validate(any(), refEq(rulesPolicy), eq("")))
            .thenReturn(listOf(violationOfMandatoryRuleTwo, violationOfNonmandatoryRule, violationOfMandatoryRuleOne))
            .thenReturn(emptyList())
        val exception = assertThrows(MojoExecutionException::class.java) {
            zallyMojo.execute()
        }
        assertEquals("Validation failed!", exception.message)
        log.result.toString().matches(expectedLogOne)
    }

    private val expectedLogTwo = """
        \[info] 
        \[info] Path: .+1\\.txt
        \[info] Spec follows the rules
        \[info] 
        \[info] Path: .+2\\.txt
        \[warn] Spec breaks the rules:
        \[warn] {3}Violations of a rule ${violationOfNonmandatoryRule.id} (see ${violationOfNonmandatoryRule.url}) :
        \[warn] {5}- pointer: ${violationOfNonmandatoryRule.pointer}
        \[warn] {7}description: ${violationOfNonmandatoryRule.description}
        \[info] 
        \[info] Overall results:
        \[info] Violations of nonmandatory rules: 1
        \[info] Violations of mandatory rules: 0
        
    """.trimIndent().toRegex()

    @Test
    fun `should not throw exception, because specs have no violations of mandatory (MUST) rules`() {
        val expectedSpecOne = """
            11
            12
            13
        """.trimIndent()
        val expectedSpecTwo = """
            21
            22
            23
        """.trimIndent()
        whenever(apiValidator.validate(eq(expectedSpecOne), refEq(rulesPolicy), eq("")))
            .thenReturn(emptyList())
        whenever(apiValidator.validate(eq(expectedSpecTwo), refEq(rulesPolicy), eq("")))
            .thenReturn(singletonList(violationOfNonmandatoryRule))
        zallyMojo.execute()
        log.result.toString().matches(expectedLogTwo)
    }

    private class TestLog : Log {

        val result: StringBuilder = java.lang.StringBuilder()

        override fun isDebugEnabled(): Boolean = true

        override fun debug(content: CharSequence?) {
        }

        override fun debug(content: CharSequence?, error: Throwable?) {
        }

        override fun debug(content: Throwable?) {
        }

        override fun isInfoEnabled(): Boolean = true

        override fun info(content: CharSequence?) {
            result.append("[info] ").append(content).append("\n")
        }

        override fun info(content: CharSequence?, error: Throwable?) {
        }

        override fun info(content: Throwable?) {
        }

        override fun isWarnEnabled(): Boolean = true

        override fun warn(content: CharSequence?) {
            result.append("[warn] $content\n")
        }

        override fun warn(content: CharSequence?, error: Throwable?) {
        }

        override fun warn(content: Throwable?) {
        }

        override fun isErrorEnabled(): Boolean = true

        override fun error(content: CharSequence?) {
            result.append("[error] $content\n")
        }

        override fun error(content: CharSequence?, error: Throwable?) {
        }

        override fun error(content: Throwable?) {
        }
    }
}
