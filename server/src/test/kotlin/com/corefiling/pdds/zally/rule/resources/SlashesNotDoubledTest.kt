package com.corefiling.pdds.zally.rule.resources

import io.swagger.models.Swagger
import io.swagger.parser.SwaggerParser
import org.assertj.core.api.Assertions
import org.junit.Test

class SlashesNotDoubledTest {

    val cut = SlashesNotDoubled()

    @Test
    fun withEmptyReturnsNull() {
        Assertions.assertThat(cut.validate(Swagger())).isNull()
    }

    @Test
    fun withSingleSlashesReturnsNull() {
        val yaml = """
            swagger: '2.0'
            info:
              title: API Title
              version: 1.0.0
            paths:
              '/some/path':
            """
        Assertions.assertThat(cut.validate(SwaggerParser().parse(yaml))).isNull()
    }

    @Test
    fun withDoubleSlashesReturnsResource() {
        val yaml = """
            swagger: '2.0'
            info:
              title: API Title
              version: 1.0.0
            paths:
              '/some//path':
                get:
            """
        Assertions.assertThat(cut.validate(SwaggerParser().parse(yaml))!!.paths)
                .hasSameElementsAs(listOf("/some//path"))
    }
}