package com.corefiling.zally.rule.collections

import com.corefiling.zally.rule.CoreFilingRuleSet
import io.swagger.models.Swagger
import io.swagger.parser.SwaggerParser
import org.assertj.core.api.Assertions
import org.junit.Test

class CollectionsArePluralTest {

    val cut = CollectionsArePlural(CoreFilingRuleSet())

    @Test
    fun withEmptyReturnsNull() {
        Assertions.assertThat(cut.validate(Swagger())).isNull()
    }

    @Test
    fun withAlreadyPluralReturnsNull() {
        val yaml = """
swagger: '2.0'
info:
  title: API Title
  version: 1.0.0
paths:
  '/things':
    get:
      responses:
        '200':
          description: describe me
          schema:
            type: array
            items:
              type: string
"""
        Assertions.assertThat(cut.validate(SwaggerParser().parse(yaml))).isNull()
    }

    @Test
    fun withAlreadyPluralButTrailingSlashReturnsNull() {
        val yaml = """
swagger: '2.0'
info:
  title: API Title
  version: 1.0.0
paths:
  '/things/':
    get:
      responses:
        '200':
          description: describe me
          schema:
            type: array
            items:
              type: string
"""
        Assertions.assertThat(cut.validate(SwaggerParser().parse(yaml))).isNull()
    }

    @Test
    fun withSingularReturnsResource() {
        val yaml = """
swagger: '2.0'
info:
  title: API Title
  version: 1.0.0
paths:
  '/thing':
    get:
      responses:
        '200':
          description: describe me
          schema:
            type: array
            items:
              type: string
"""
        Assertions.assertThat(cut.validate(SwaggerParser().parse(yaml))!!.paths)
                .hasSameElementsAs(listOf("paths /thing: 'thing' appears to be singular"))
    }

    @Test
    fun withSingularReturnsResourceWithWordPickedOut() {
        val yaml = """
swagger: '2.0'
info:
  title: API Title
  version: 1.0.0
paths:
  '/path/to/taxonomy-package/':
    get:
      responses:
        '200':
          description: describe me
          schema:
            type: array
            items:
              type: string
"""
        Assertions.assertThat(cut.validate(SwaggerParser().parse(yaml))!!.paths)
                .hasSameElementsAs(listOf("paths /path/to/taxonomy-package/: 'package' appears to be singular"))
    }
}