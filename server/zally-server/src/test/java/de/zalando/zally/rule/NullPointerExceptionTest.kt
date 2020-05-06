package de.zalando.zally.rule

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import de.zalando.zally.apireview.RestApiTestConfiguration.Companion.assertRuleManagerUsingAllAnnotatedRules
import de.zalando.zally.core.CompositeRulesValidator
import de.zalando.zally.core.EMPTY_JSON_POINTER
import de.zalando.zally.core.ObjectTreeReader
import de.zalando.zally.core.RulesManager
import de.zalando.zally.core.RulesPolicy
import de.zalando.zally.core.plus
import de.zalando.zally.core.toEscapedJsonPointer
import io.swagger.util.Yaml
import org.intellij.lang.annotations.Language
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.rules.SpringClassRule
import org.springframework.test.context.junit4.rules.SpringMethodRule

@RunWith(Parameterized::class)
@SpringBootTest
@ActiveProfiles("test", "all-annotated-rules")
class NullPointerExceptionTest(
    private val name: String,
    private val spec: String
) {
    companion object {
        @ClassRule
        @JvmField
        val springClassRule = SpringClassRule()

        @Parameterized.Parameters(name = "{0}")
        @JvmStatic
        fun parameters(): Iterable<Array<String>> = (
            parametersFromFullFeaturedSpec() +
            parametersFromPetstore()
        )
        .asIterable()

        private fun JsonNode.pretty(): String = Yaml.pretty().writeValueAsString(this)

        private fun parametersFromFullFeaturedSpec(): Sequence<Array<String>> {
            @Language("YAML")
            val spec = """
                openapi: '3.0.1'

                info:
                  title: Full Featured API
                  description: API using all OpenAPI features
                  termsOfService: http://example.com/tos
                  contact:
                    name: Example Contact
                    url: http://example.com/contact
                    email: contact@example.com
                  license:
                    name: BSD
                    url: http://example.com/license
                  version:

                servers:
                  - url: '{scheme}://example.com/'
                    description: Example Server
                    variables:
                      scheme:
                        description: Example scheme
                        enum:
                          - 'https'
                          - 'http'
                        default: 'https'

                security:
                - api_key: []
                - petstore_auth:
                  - write:pets
                  - read:pets

                tags:
                - ExampleTag:
                    name: ExampleTag title
                    description: ExampleTag described
                    externalDocs:
                      description: Example Tag Docs
                      url: http://example.com/docs/ExampleTag

                externalDocs:
                  description: Example Tag Docs
                  url: http://example.com/docs/ExampleTag

                x-extension: Extension features

                components:
                  schemas:
                    ExampleSchema:
                      title: Title
                      description: Description
                      format: format
                      multipleOf: multipleOf?
                      maximum: 100
                      exclusiveMaximum: 0
                      minimum: 0
                      exclusiveMinimum: 100
                      maxLength: 0
                      minLength: 100
                      pattern: \d+
                      maxItems: 0
                      minItems: 0
                      uniqueItems: true
                      maxProperties: 100
                      minProperties: 0
                      required: true
                      enum: enum
                      type: object
                      default: default
                      nullable: true
                      discriminator: disc
                      readOnly: true
                      writeOnly: true
                      example: {}
                      deprecated: false
                      allOf:
                        - type: object
                      anyOf:
                        - type: object
                      oneOf:
                        - type: object
                      not:
                        type: object
                      properties:
                        id:
                          type: integer
                          format: int64
                        name:
                          type: string

                  securitySchemes:
                    Scheme1:
                      type: http
                      description: Desc
                      name: name
                      in: header
                      scheme: http
                      bearerFormat: http
                      openidConnectUrl: http://login.example.com
                      flows:
                        implicit:
                          tokenUrl: http://login.example.com
                          refreshUrl: http://login.example.com
                          authorizationUrl: http://login.example.com
                          scopes:
                            key: value
                        password:
                          tokenUrl: http://login.example.com
                          refreshUrl: http://login.example.com
                          authorizationUrl: http://login.example.com
                          scopes:
                            key: value
                        clientCredentials:
                          tokenUrl: http://login.example.com
                          refreshUrl: http://login.example.com
                          authorizationUrl: http://login.example.com
                          scopes:
                            key: value
                        authorizationCode:
                          tokenUrl: http://login.example.com
                          refreshUrl: http://login.example.com
                          authorizationUrl: http://login.example.com
                          scopes:
                            key: value

                  parameters:
                    ExampleParameter:
                      name: ExampleParameter
                      in: query
                      description: Example parameter description
                      required: false
                      deprecated: false
                      allowEmptyValues: true
                      style: form
                      explode: true
                      allowReserved: true
                      schema:
                        type: object
                      example: {}
                      examples:
                        AnExample:
                          summary: Summarized
                          description: Described
                          value: The value
                          externalValue: http://example.com

                  headers:
                    ExampleHeader:
                      description: Example parameter description
                      required: false
                      deprecated: false
                      allowEmptyValues: true
                      style: form
                      explode: true
                      allowReserved: true
                      schema:
                        type: object
                      example: {}
                      examples:
                        AnExample:
                          summary: Summarized
                          description: Described
                          value: The value
                          externalValue: http://example.com

                  links:
                    ExampleLink:
                      operationRef: ref
                      operationId: id
                      parameters:
                        key: value
                      requestBody: body
                      description: description
                      server:
                        url: http://example.com
                        description: Example server

                  requestBodies:
                    ExampleRequest:
                      description: A description
                      required: true
                      content:
                        schema:
                          type: object
                        example: example
                        encoding:
                          propName:
                            contentType: binary
                            headers:
                              X-Rate-Limit-Limit:
                                description: The number of allowed requests in the current period
                                schema:
                                  type: integer
                            style: style
                            explode: true
                            allowReserved: false

                  responses:
                    ExampleResponse:
                      description: Response described
                      headers: []
                      content:
                        schema:
                          type: object
                        example: example
                        encoding:
                          propName:
                            contentType: binary
                            headers:
                              X-Rate-Limit-Limit:
                                description: The number of allowed requests in the current period
                                schema:
                                  type: integer
                            style: style
                            explode: true
                            allowReserved: false
                      links:
                        ExampleLink:
                          operationRef: ref
                          operationId: id
                          parameters:
                            key: value
                          requestBody: body
                          description: description
                          server:
                            url: http://example.com
                            description: Example server

                  examples:
                    AnExample:
                      summary: Summarized
                      description: Described
                      value: The value
                      externalValue: http://example.com

                  callbacks:
                    myWebhook:
                      'http://notificationServer.com?transactionId={${'$'}request.body#/id}&email={${'$'}request.body#/email}':
                        post:
                          requestBody:
                            description: Callback payload
                            content:
                              'application/json':
                                schema:
                                  ${'$'}ref: '#/components/schemas/SomePayload'
                          responses:
                            '200':
                              description: webhook successfully processed and no retries will be performed

                paths:
                  /somepath:
                    ${'$'}ref: '#/components/schemas/SomePayload'
                    description: Described
                    put:
                      tags: [Tag1]
                      summary: Summary
                      description: Descriptoin
                      operationId: op id
                      parameters:
                      - name: Parameter name
                      deprecated: false
                      servers:
                      - url: 'https://example.com/'
                        description: Example Server
                      externalDocs:
                        url: http://example.com/docs/
                        description: External docs
                      requestBody:
                        description: user to add to the system
                        content:
                          'application/json':
                            schema:
                              ${'$'}ref: '#/components/schemas/User'
                            examples:
                              user:
                                summary: User Example
                                externalValue: 'http://foo.bar/examples/user-example.json'
                          'application/xml':
                            schema:
                              ${'$'}ref: '#/components/schemas/User'
                            examples:
                              user:
                                summary: User Example in XML
                                externalValue: 'http://foo.bar/examples/user-example.xml'
                          'text/plain':
                            examples:
                              user:
                                summary: User example in text plain format
                                externalValue: 'http://foo.bar/examples/user-example.txt'
                          '*/*':
                            examples:
                              user:
                                summary: User example in other format
                                externalValue: 'http://foo.bar/examples/user-example.whatever'
                      responses:
                        default:
                          description: A complex object array response
                          content:
                            application/json:
                              schema:
                                type: array
                                items:
                                  ${'$'}ref: '#/components/schemas/VeryComplexType'
                      callbacks:
                        ${'$'}ref: '#/components/callbacks/SomeCallback'
                      security:
                        petstore_auth:
                        - write:pets
                        - read:pets
                    get: {}
                    post: {}
                    head: {}
                    patch: {}
                    trace: {}
                    delete: {}
                    options: {}
                    servers:
                    - url: 'https://example.com/'
                      description: Example Server
                    parameters:
                    - name: Parameter name

                """.trimIndent()
            return parameters("full", spec)
        }

        private fun parametersFromPetstore(): Sequence<Array<String>> =
            parameters("petstore", NullPointerExceptionTest::class.java.getResource("/fixtures/openapi3_petstore.yaml").readText())

        private fun parameters(name: String, spec: String): Sequence<Array<String>> {
            val root = ObjectTreeReader().read(spec)
            return sequenceOf(arrayOf("$name unmodified", spec)) +
            root.allJsonPointers().reversed().asSequence().flatMap { pointer ->
                pointer.head()?.let { head ->
                    val last = pointer.last()
                    val parent = root.at(head)
                    when (parent) {
                        is ObjectNode -> {
                            parent.set<ObjectNode>(last.matchingProperty, null)
                            val param1 = arrayOf("$name with null $pointer", root.pretty())

                            parent.remove(last.matchingProperty)
                            val param2 = arrayOf("$name with removed $pointer", root.pretty())

                            sequenceOf(param1, param2)
                        }
                        is ArrayNode -> {
                            parent.set(last.matchingIndex, null)
                            val param1 = arrayOf("$name with null $pointer", root.pretty())

                            parent.remove(last.matchingIndex)
                            val param2 = arrayOf("$name with removed $pointer", root.pretty())

                            sequenceOf(param1, param2)
                        }
                        else -> emptySequence()
                    }
                }.orEmpty()
            }
        }

        private fun JsonNode?.allJsonPointers(): List<JsonPointer> =
            listOf(EMPTY_JSON_POINTER) +
            when (this) {
                is ObjectNode -> {
                    fields().asSequence().toList().flatMap { (name, node) ->
                        node.allJsonPointers().map {
                            EMPTY_JSON_POINTER + name.toEscapedJsonPointer() + it
                        }
                    }
                }
                is ArrayNode -> {
                    (0 until size()).flatMap { index ->
                        get(index).allJsonPointers().map {
                            EMPTY_JSON_POINTER + index.toString().toEscapedJsonPointer() + it
                        }
                    }
                }
                else -> emptyList<JsonPointer>()
            }
    }

    @Rule
    @JvmField
    final val springMethodRule = SpringMethodRule()

    @Autowired
    private lateinit var validator: CompositeRulesValidator

    @Autowired
    private lateinit var rules: RulesManager

    @Test
    fun `validate with spec does not throw NullPointerException`() {
        assertRuleManagerUsingAllAnnotatedRules(rules)

        validator.validate(spec, RulesPolicy(emptyList()))
    }
}
