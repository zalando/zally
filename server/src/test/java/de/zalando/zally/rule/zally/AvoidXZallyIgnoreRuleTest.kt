package de.zalando.zally.rule.zally

import de.zalando.zally.rule.ObjectTreeReader
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

/**
 * Tests for AvoidXZallyIgnoreRule
 */
class AvoidXZallyIgnoreRuleTest {

    private val rule = AvoidXZallyIgnoreRule()
    private val reader = ObjectTreeReader()

    /** Validates spec with no x-zally-ignore results in no violation */
    @Test
    fun validateSpecWithNoIgnores() {
        val root = reader.read("""
            swagger: 2.0
            """.trimIndent())

        val violation = rule.validate(root)

        assertThat(violation).isNull()
    }

    /** Validates spec with x-zally-ignore inline at root results in violation */
    @Test
    @Suppress("UnsafeCallOnNullableType")
    fun validateSpecWithInlineIgnoresAtRoot() {
        val root = reader.read("""
            swagger: 2.0
            x-zally-ignore: [ ONE, TWO, THREE]
            """.trimIndent())

        val violation = rule.validate(root)

        assertThat(violation?.paths!!)
                .hasSameElementsAs(listOf("ignores rules ONE, TWO, THREE"))
    }

    /** Validates spec with x-zally-ignore alternate syntax at root results in violation */
    @Test
    fun validateSpecWithDashedIgnoresAtRoot() {
        val root = reader.read("""
            swagger: 2.0
            x-zally-ignore:
              - A
              - BB
              - CCC
            """.trimIndent())

        val violation = rule.validate(root)

        assertThat(violation?.paths!!)
                .hasSameElementsAs(listOf("ignores rules A, BB, CCC"))
    }

    /** Validates spec with x-zally-ignore deeper within the model results in violation */
    @Test
    fun validateSpecWithInlineIgnoresDeeper() {
        val root = reader.read("""
            swagger: 2.0
            info:
              x-zally-ignore: [ X, YY, ZZZ]
            """.trimIndent())

        val violation = rule.validate(root)

        assertThat(violation?.paths!!)
                .hasSameElementsAs(listOf("info: ignores rules X, YY, ZZZ"))
    }

    /** Validates spec with x-zally-ignore specifying value rather than list results in violation */
    @Test
    fun validateSpecWithInvalidSingleValueIgnores() {
        val root = reader.read("""
            swagger: 2.0
            x-zally-ignore: INVALID
            """.trimIndent())

        val violation = rule.validate(root)

        assertThat(violation?.paths!!)
                .hasSameElementsAs(listOf("invalid ignores, expected list but found single value \"INVALID\""))
    }

    /** Validates spec with x-zally-ignore specifying an object rather than list results in violation */
    @Test
    fun validateSpecWithInvalidOtherIgnores() {
        val root = reader.read("""
            swagger: 2.0
            x-zally-ignore:
              invalid: INVALID
            """.trimIndent())

        val violation = rule.validate(root)

        assertThat(violation?.paths!!)
                .hasSameElementsAs(listOf("invalid ignores, expected list but found {\"invalid\":\"INVALID\"}"))
    }
}
