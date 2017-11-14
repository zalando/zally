package de.zalando.zally.rule.zalando

import io.swagger.models.Info
import io.swagger.models.Swagger
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class VersionInInfoSectionRuleTest {

    private fun swaggerWithVersion(versionValue: String) =
        Swagger().apply {
            info = Info().apply {
                version = versionValue
            }
        }

    private val rule = VersionInInfoSectionRule(ZalandoRuleSet())

    @Test
    fun emptySwagger() {
        val result = rule.validate(Swagger())!!
        assertThat(result.description).contains("Version is missing")
    }

    @Test
    fun missingVersion() {
        val swagger = Swagger().apply { info = Info() }
        val result = rule.validate(swagger)!!
        assertThat(result.description).contains("Version is missing")
    }

    @Test
    fun wrongVersionFormat() {
        val swagger = swaggerWithVersion("1.2.3-a")
        val result = rule.validate(swagger)!!
        assertThat(result.description).contains("Specified version has incorrect format")
    }

    @Test
    fun correctVersion() {
        val swagger = swaggerWithVersion("1.2.3")
        assertThat(rule.validate(swagger)).isNull()
    }
}
