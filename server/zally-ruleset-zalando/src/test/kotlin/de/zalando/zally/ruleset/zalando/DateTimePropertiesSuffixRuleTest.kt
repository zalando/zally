@file:Suppress("YamlSchema")

package de.zalando.zally.ruleset.zalando

import com.typesafe.config.ConfigValueFactory
import de.zalando.zally.core.rulesConfig
import de.zalando.zally.core.DefaultContextFactory
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.Test

class DateTimePropertiesSuffixRuleTest {

    private val rule = DateTimePropertiesSuffixRule(rulesConfig)

    @Test
    fun `rule should pass with correct "date-time" fields`() {
        @Language("YAML")
        val content = """
            openapi: '3.0.1'
            info:
              title: Test API
              version: 1.0.0
            components:
              schemas:
                Pet:
                  properties:
                    created_at:
                      type: string
                      format: date-time
                    modified_at:
                      type: string
                      format: date-time                      
                    occurred_at:
                      type: string
                      format: date-time                      
                    returned_at:
                      type: string
                      format: date-time
            """.trimIndent()
        val violations = rule.validate(DefaultContextFactory().getOpenApiContext(content))
        assertThat(violations).isEmpty()
    }

    @Test
    fun `rule should pass with correct "date" fields`() {
        @Language("YAML")
        val content = """
            openapi: '3.0.1'
            info:
              title: Test API
              version: 1.0.0
            components:
              schemas:
                Car:
                  properties:
                    created_at:
                      type: string
                      format: date
                    modified_at:
                      type: string
                      format: date                      
                    occurred_at:
                      type: string
                      format: date                      
                    returned_at:
                      type: string
                      format: date                  
            """.trimIndent()
        val violations = rule.validate(DefaultContextFactory().getOpenApiContext(content))
        assertThat(violations).isEmpty()
    }

    @Test
    fun `should ignore fields with non-date and time types`() {
        @Language("YAML")
        val content = """
            openapi: '3.0.1'
            info:
              title: Test API
              version: 1.0.0
            components:
              schemas:
                Car:
                  properties:
                    created:
                      type: string                      
                    occurred:
                      type: string                      
                    returned:
                      type: string                      
                    modified:
                      type: int                                          
            """.trimIndent()
        val violations = rule.validate(DefaultContextFactory().getOpenApiContext(content))
        assertThat(violations).isEmpty()
    }

    @Test
    fun `rule should fail to validate schema`() {
        @Language("YAML")
        val content = """
            openapi: '3.0.1'
            info:
              title: Test API
              version: 1.0.0
            components:
              schemas:
                Car:
                  properties:
                    created:
                      type: string
                      format: date-time
                    occurred:
                      type: string
                      format: date
                    returned:
                      type: string
                      format: date-time
                    modified:
                      type: string
                      format: date
            """.trimIndent()
        val violations = rule.validate(DefaultContextFactory().getOpenApiContext(content))
        assertThat(violations.map { it.description }).containsExactly(
            rule.generateMessage("created", "string", "date-time"),
            rule.generateMessage("occurred", "string", "date"),
            rule.generateMessage("returned", "string", "date-time"),
            rule.generateMessage("modified", "string", "date")
        )
    }

    @Test
    fun `rule should support different patterns`() {
        @Language("YAML")
        val content = """
            openapi: '3.0.1'
            info:
              title: Test API
              version: 1.0.0
            components:
              schemas:
                Car:
                  properties:
                    created:
                      type: string
                      format: date-time
                    modified:
                      type: string
                      format: date
            """.trimIndent()
        val newConfig = rulesConfig.withValue("DateTimePropertiesSuffixRule/patterns", ConfigValueFactory.fromIterable(listOf("was_.*")))
        val customRule = DateTimePropertiesSuffixRule(newConfig)
        val violations = customRule.validate(DefaultContextFactory().getOpenApiContext(content))
        assertThat(violations.map { it.description }).containsExactly(
            customRule.generateMessage("created", "string", "date-time"),
            customRule.generateMessage("modified", "string", "date")
        )
    }
}
