package org.zalando.zally.ruleset.zally

import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.zalando.zally.core.DefaultContextFactory
import org.zalando.zally.test.ZallyAssertions.assertThat

class PathParameterRuleTest {

    private val rule = PathParameterRule()

    @Test
    internal fun `return violation if a path parameter is not marked as 'required'`() {
        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: '3.0.0'
            info:
              title: API 1
              contact: 
                info: "Team One"               
            paths:
              /items/{item-id}:
                get:
                  parameters:
                  - name: item-id
                    in: path                  
                    description: The id of the pet to retrieve
                    schema:
                      type: string
                  responses: 
                    default:
                     description: Response
                     content:                        
                       application/json:
                         schema: 
                           type: string
            """.trimIndent()
        )

        val violations = rule.checkRequiredPathAttribute(context)

        assertThat(violations)
            .descriptionsAllEqualTo(PathParameterRule.REQUIRED_ATTRIBUTE_ERROR_MESSAGE)
            .pointersEqualTo("/paths/~1items~1{item-id}/get/parameters/0")
    }

    @Test
    fun `return no violations if a path parameter marked as 'required'`() {
        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: '3.0.0'
            info:
              title: API 1
              contact: 
                info: "Team One"               
            paths:
              /items/{item-id}:
                get:
                  parameters:
                  - name: item-id
                    in: path
                    required: true
                    description: The id of the pet to retrieve
                    schema:
                      type: string
                  responses: 
                    default:
                     description: Response
                     content:                        
                       application/json:
                         schema: 
                           type: string
            """.trimIndent()
        )
        val violations = rule.checkRequiredPathAttribute(context)
        assertThat(violations).isEmpty()
    }

    @Test
    internal fun `return violation if a path parameter in components is not marked as 'required'`() {
        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: '3.0.0'
            info:
              title: API 1
              contact: 
                info: "Team One"               
            paths:
              /items/{item-id}:
                get:
                  parameters:
                    - $\ref: "#/components/parameters/QueryParameter"
                  responses: 
                    default:
                     description: Response
                     content:                        
                       application/json:
                         schema: 
                           type: string
            components: 
              parameters:
                QueryParameter:
                  name: item-id
                  in: path                  
                  description: The id of the pet to retrieve
                  schema:
                    type: string
            """.trimIndent()
        )

        val violations = rule.checkRequiredPathAttribute(context)

        assertThat(violations)
            .descriptionsAllEqualTo(PathParameterRule.REQUIRED_ATTRIBUTE_ERROR_MESSAGE)
            .pointersEqualTo("/components/parameters/QueryParameter")
    }

    @Test
    fun `return violation if parameter has no 'schema' and 'content' defined`() {
        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: '3.0.0'
            info:
              title: Schema and content Parameter properties validation
              contact: 
                info: "Team One"               
            paths:
              /endpoint:
                post:
                  summary: |
                    Some summary.
                  security:
                    - oauth2: ["uid"]
                  parameters:
                    - name: X-HEADER-ID
                      description: |
                        Header description
                      in: header
                      required: false
                    - $\ref: "#/components/parameters/QueryParameter"
            components: 
              parameters:
                QueryParameter:
                  name: item-id
                  in: path                  
                  description: The id of the pet to retrieve
            """.trimIndent()
        )

        val violations = rule.checkSchemaOrContentProperty(context)

        assertThat(violations).hasSize(2)
        assertThat(violations).containsDescriptionsInAnyOrder(
            PathParameterRule.requiredSchemaOrContentErrorMessage("X-HEADER-ID"),
            PathParameterRule.requiredSchemaOrContentErrorMessage("item-id")
        )
    }

    @Test
    fun `return no violations if either 'schema' or 'content' are present`() {
        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: '3.0.0'
            info:
              title: Schema and content Parameter properties validation
              contact: 
                info: "Team One"               
            paths:
              /endpoint:
                post:
                  summary: |
                    Some summary.
                  security:
                    - oauth2: ["uid"]
                  parameters:
                    - name: X-HEADER-ID
                      description: Header description
                      in: header
                      required: false
                      content:
                        application/json:
                          schema:
                            type: object
                            properties: 
                              x:
                                type: string
                    - $\ref: "#/components/parameters/QueryParameter"
            components: 
              parameters:
                QueryParameter:
                  name: item-id
                  in: path                  
                  description: The id of the pet to retrieve
                  schema:
                    type: string
            """.trimIndent()
        )

        val violations = rule.checkSchemaOrContentProperty(context)

        assertThat(violations).isEmpty()
    }

    @Test
    fun `return violation if 'content' field contains more than one key`() {
        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: '3.0.0'
            info:
              title: Schema and content Parameter properties validation
              contact: 
                info: "Team One"               
            paths:
              /endpoint:
                post:
                  summary: |
                    Some summary.
                  security:
                    - oauth2: ["uid"]
                  parameters:
                    - name: X-VALID-HEADER
                      description: Header description
                      in: header
                      required: false
                      content:
                        application/json:
                          schema:
                            type: object
                            properties: 
                              x:
                                type: string
                    - name: X-HEADER-ID
                      description: Header description
                      in: header
                      required: false
                      content:
                        application/json:
                          schema:
                            type: object
                            properties: 
                              x:
                                type: string
                        application/xml:
                          schema:
                            type: object
                            properties: 
                              x:
                                type: string
                    - $\ref: "#/components/parameters/QueryParameter"
            components: 
              parameters:
                QueryParameter:
                  name: item-id
                  in: path                  
                  description: The id of the pet to retrieve
                  content: 
                  
            """.trimIndent()
        )

        val violations = rule.validateParameterContentMapStructure(context)

        assertThat(violations).hasSize(1)
        assertThat(violations).descriptionsAllEqualTo(
            PathParameterRule.contentMapStructureErrorMessage("X-HEADER-ID")
        )
    }

    @Test
    fun `return no violations if 'content' is not defined`() {
        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: '3.0.0'
            info:
              title: Schema and content Parameter properties validation
              contact: 
                info: "Team One"               
            paths:
              /endpoint:
                post:
                  summary: |
                    Some summary.
                  security:
                    - oauth2: ["uid"]
                  parameters:
                    - name: X-HEADER-ID
                      description: Header description
                      in: header
                      required: false
                      schema:
                        type: string
                    - $\ref: "#/components/parameters/QueryParameter"
            components: 
              parameters:
                QueryParameter:
                  name: item-id
                  in: path                  
                  description: The id of the pet to retrieve
                  schema:
                    type: string
            """.trimIndent()
        )

        val violations = rule.validateParameterContentMapStructure(context)

        assertThat(violations).isEmpty()
    }

    @Test
    fun `return no violations if parameter ref is not correct`() {
        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: '3.0.0'
            info:
              title: Schema and content Parameter properties validation
              contact: 
                info: "Team One"               
            paths:
              /endpoint:
                post:
                  summary: |
                    Some summary.
                  security:
                    - oauth2: ["uid"]
                  parameters:
                    - $\ref: "https://invalid-url/X-Flow-ID"
            """.trimIndent()
        )

        val violations = rule.checkSchemaOrContentProperty(context)

        assertThat(violations).isEmpty()
    }
}
