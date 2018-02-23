package com.corefiling.pdds.zally.rule.collections

import io.swagger.models.Swagger
import io.swagger.parser.SwaggerParser
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CollectionsReturnArraysTest {

    val cut = CollectionsReturnArrays()

    @Test
    fun withEmptyReturnsNull() {
        assertThat(cut.validate(Swagger())).isNull()
    }

    @Test
    fun withAlreadyReturningArrayReturnsNull() {
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
        assertThat(cut.validate(SwaggerParser().parse(yaml))).isNull()
    }

    @Test
    fun withNotReturningArrayReturnsResource() {
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
            type: object
  '/things/{id}':
    parameters:
      - name: id
        in: path
        type: string
        required: true
"""
        assertThat(cut.validate(SwaggerParser().parse(yaml))!!.paths)
                .hasSameElementsAs(listOf("/things GET response 200 schema type: expected array but found object"))
    }
}
