package de.zalando.zally.ruleset.zalando

import de.zalando.zally.core.DefaultContextFactory
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.Test

class VersionInInfoSectionRuleTest {

    private val rule = VersionInInfoSectionRule()

    @Test
    fun `checkAPIVersion should return violation if version is not set`() {
        @Language("YAML")
        val spec = """
            openapi: 3.0.1
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violation = rule.checkAPIVersion(context)

        assertThat(violation).isNotNull
        assertThat(violation!!.description).contains("version is missing")
        assertThat(violation.pointer.toString()).isEqualTo("/info/version")
    }

    @Test
    fun `checkAPIVersion should return violation if version format is incorrect`() {
        @Language("YAML")
        val spec = """
            openapi: 3.0.1
            info:
              version: 1-alpha
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violation = rule.checkAPIVersion(context)

        assertThat(violation).isNotNull
        assertThat(violation!!.description).contains("incorrect format")
        assertThat(violation.pointer.toString()).isEqualTo("/info/version")
    }

    @Test
    fun `checkAPIVersion should return no violation if version is present in the correct format`() {
        @Language("YAML")
        val spec = """
            openapi: 3.0.1
            info:
              version: 1.0.0
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violation = rule.checkAPIVersion(context)

        assertThat(violation).isNull()
    }

    @Test
    fun `versionRegex should match only valid version format`() {
        assertThat("1.0.0".matches(rule.versionRegex)).isTrue()
        assertThat("111.222.333".matches(rule.versionRegex)).isTrue()
        assertThat("1.0".matches(rule.versionRegex)).isTrue()

        assertThat(".0.0".matches(rule.versionRegex)).isFalse()
        assertThat("..".matches(rule.versionRegex)).isFalse()
        assertThat("1.".matches(rule.versionRegex)).isFalse()
        assertThat("1-some-version".matches(rule.versionRegex)).isFalse()
        assertThat("this-is-not-a-valid-version".matches(rule.versionRegex)).isFalse()
        assertThat("1.1.1.1.1.1.1".matches(rule.versionRegex)).isFalse()
    }
}
