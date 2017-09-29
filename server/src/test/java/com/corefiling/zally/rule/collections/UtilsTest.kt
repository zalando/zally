package com.corefiling.zally.rule.collections

import io.swagger.models.Swagger
import io.swagger.parser.SwaggerParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UtilsTest {

    @Test
    fun collectionPathsWithNullReturnsNull() {
        assertNull(collectionPaths(null))
    }

    @Test
    fun collectionPathsWithDefaultReturnsNull() {
        assertNull(collectionPaths(Swagger()))
    }

    @Test
    fun collectionPathsWithResourceReturningArrayReturnsResource() {
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
        val result = collectionPaths(SwaggerParser().parse(yaml))

        assertEquals("[/things]", result?.keys?.toString() ?: "")
    }

    @Test
    fun collectionPathsWithResourceReturningReferencedArrayReturnsResource() {
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
          description: 'describe me'
          schema:
            ${'$'}ref: '#/definitions/ArrayOfThings'
definitions:
  ArrayOfThings:
    type: array
    items:
      type: string
"""
        val result = collectionPaths(SwaggerParser().parse(yaml))

        assertEquals("[/things]", result?.keys?.toString() ?: "")
    }

    @Test
    fun collectionPathsWithParameterizedSubresourcesReturnsParents() {
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
          description: 'describe me'
          schema:
            type: object
  '/things/{id}':
    parameters:
      - name: id
        in: path
        type: string
        required: true
    get:
      responses:
        '200':
          description: Get just one thing
"""
        val result = collectionPaths(SwaggerParser().parse(yaml))

        assertEquals("[/things]", result?.keys?.toString() ?: "")
    }

    @Test
    fun collectionPathsWithPageSizeQueryParameterReturnsResource() {
        val yaml = """
swagger: '2.0'
info:
  title: API Title
  version: 1.0.0
paths:
  '/things':
    get:
      parameters:
        - ${'$'}ref: '#/parameters/pageSize'
      responses:
        '200':
          description: 'describe me'
          schema:
            type: object
parameters:
  pageSize:
    name: pageSize
    in: query
    type: integer
"""
        val result = collectionPaths(SwaggerParser().parse(yaml))

        assertEquals("[/things]", result?.keys?.toString() ?: "")
    }

    @Test
    fun collectionPathsWithNoPaginatedResourcesReturnsEmpty() {
        val yaml = """
swagger: '2.0'
info:
  title: API Title
  version: 1.0.0
paths:
  '/status':
    get:
      responses:
        '200':
          description: 'describe me'
          schema:
            type: object
"""
        val result = collectionPaths(SwaggerParser().parse(yaml))

        assertEquals("[]", result?.keys?.toString() ?: "")
    }
}
