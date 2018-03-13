package de.zalando.zally.rule.zally

import de.zalando.zally.rule.ObjectTreeReader
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AvoidXZallyIgnoresRuleTest {

    private val rule = AvoidXZallyIgnoresRule()
    private val reader = ObjectTreeReader()

    @Test
    fun validateSpecWithNoIgnores() {
        val root = reader.read("""
            swagger: 2.0
            """.trimIndent())

        val violation = rule.validate(root)

        assertThat(violation).isNull()
    }

    @Test
    fun validateSpecWithInlineIgnoresAtRoot() {
        val root = reader.read("""
            swagger: 2.0
            x-zally-ignores: [ ONE, TWO, THREE]
            """.trimIndent())

        val violation = rule.validate(root)

        assertThat(violation?.paths!!)
                .hasSameElementsAs(listOf("ignores rules ONE, TWO, THREE"))
    }

    @Test
    fun validateSpecWithDashedIgnoresAtRoot() {
        val root = reader.read("""
            swagger: 2.0
            x-zally-ignores:
              - ONE
              - TWO
              - THREE
            """.trimIndent())

        val violation = rule.validate(root)

        assertThat(violation?.paths!!)
                .hasSameElementsAs(listOf("ignores rules ONE, TWO, THREE"))
    }

    @Test
    fun validateSpecWithInlineIgnoresDeeper() {
        val root = reader.read("""
            swagger: 2.0
            info:
              x-zally-ignores: [ ONE, TWO, THREE]
            """.trimIndent())

        val violation = rule.validate(root)

        assertThat(violation?.paths!!)
                .hasSameElementsAs(listOf("info: ignores rules ONE, TWO, THREE"))
    }

    @Test
    fun validateSpecWithInvalidSingleValueIgnores() {
        val root = reader.read("""
            swagger: 2.0
            x-zally-ignores: INVALID
            """.trimIndent())

        val violation = rule.validate(root)

        assertThat(violation?.paths!!)
                .hasSameElementsAs(listOf("invalid ignores, expected list but found single value \"INVALID\""))
    }

    @Test
    fun validateSpecWithInvalidOtherIgnores() {
        val root = reader.read("""
            swagger: 2.0
            x-zally-ignores:
              invalid: INVALID
            """.trimIndent())

        val violation = rule.validate(root)

        assertThat(violation?.paths!!)
                .hasSameElementsAs(listOf("invalid ignores, expected list but found {\"invalid\":\"INVALID\"}"))
    }
}
