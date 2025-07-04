@file:Suppress("YamlSchema")

package org.zalando.zally.ruleset.zalando

import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.zalando.zally.core.DefaultContextFactory
import org.zalando.zally.core.rulesConfig

class DateTimePropertiesSuffixRuleTest {

    private val rule = DateTimePropertiesSuffixRule(rulesConfig)

    @Test
    fun `should pass with correct format and suffix`() {
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
                    was_created_at:
                      type: string
                      format: date-time
                    was_modified_time:
                      type: string
                      format: time
                    has_occurred_timestamp:
                      type: string
                      format: date-time                      
                    was_returned_date:
                      type: string
                      format: date
                    was_delivered_day:
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
                    was_created:
                      type: string
                    was_modified:
                      type: enum
                      enum: [ "yes", "no" ]
                    has_occurred:
                      type: string                      
                    was_returned:
                      type: string                      
                    was_delivered:
                      type: int                                          
        """.trimIndent()
        val violations = rule.validate(DefaultContextFactory().getOpenApiContext(content))
        assertThat(violations).isEmpty()
    }

    @Test
    fun `should fail on suffix _at and succeed on infix patterns`() {
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
                    was_created_at_suffix:
                      type: string
                      format: date-time
                    was_modified_time_suffix:
                      type: string
                      format: time
                    has_occurred_timestamp_suffix:
                      type: string
                      format: date-time
                    was_returned_date_suffix:
                      type: string
                      format: date
                    was_delivered_day_suffix:
                      type: string
                      format: date
        """.trimIndent()
        val violations = rule.validate(DefaultContextFactory().getOpenApiContext(content))
        assertThat(violations.map { it.description }).containsExactly(
            rule.generateMessage("was_created_at_suffix", "string", "date-time")
        )
    }

    @Test
    fun `should fail on prefix at_ and succeed on prefix patterns`() {
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
                    at_created:
                      type: string
                      format: date-time
                    time_modified_suffix:
                      type: string
                      format: time
                    timestamp_occurred_suffix:
                      type: string
                      format: date-time
                    date_returned_suffix:
                      type: string
                      format: date
                    day_delivered_suffix:
                      type: string
                      format: date
        """.trimIndent()
        val violations = rule.validate(DefaultContextFactory().getOpenApiContext(content))
        assertThat(violations.map { it.description }).containsExactly(
            rule.generateMessage("at_created", "string", "date-time")
        )
    }

    @Test
    fun `should fail on leading _`() {
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
                    _at:
                      type: string
                      format: date-time
                    _time:
                      type: string
                      format: time
                    _timestamp:
                      type: string
                      format: date-time
                    _date:
                      type: string
                      format: date
                    _day:
                      type: string
                      format: date
        """.trimIndent()
        val violations = rule.validate(DefaultContextFactory().getOpenApiContext(content))
        assertThat(violations.map { it.description }).containsExactly(
            rule.generateMessage("_at", "string", "date-time"),
            rule.generateMessage("_time", "string", "time"),
            rule.generateMessage("_timestamp", "string", "date-time"),
            rule.generateMessage("_date", "string", "date"),
            rule.generateMessage("_day", "string", "date")
        )
    }

    @Test
    fun `should fail on trailing _`() {
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
                    at_:
                      type: string
                      format: date-time
                    time_:
                      type: string
                      format: time
                    timestamp_:
                      type: string
                      format: date-time
                    date_:
                      type: string
                      format: date
                    day_:
                      type: string
                      format: date
        """.trimIndent()
        val violations = rule.validate(DefaultContextFactory().getOpenApiContext(content))
        assertThat(violations.map { it.description }).containsExactly(
            rule.generateMessage("at_", "string", "date-time"),
            rule.generateMessage("time_", "string", "time"),
            rule.generateMessage("timestamp_", "string", "date-time"),
            rule.generateMessage("date_", "string", "date"),
            rule.generateMessage("day_", "string", "date")
        )
    }
}
