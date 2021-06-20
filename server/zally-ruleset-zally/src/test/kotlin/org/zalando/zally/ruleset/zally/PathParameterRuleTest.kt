package org.zalando.zally.ruleset.zally

import org.intellij.lang.annotations.Language
import org.junit.Test
import org.zalando.zally.core.DefaultContextFactory
import org.zalando.zally.test.ZallyAssertions.assertThat

class PathParameterRuleTest {

    private val rule = PathParameterRule()

    @Test
    internal fun `return violation if a path parameter is not marked as "required"`() {
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

        val violations = rule.validate(context)

        assertThat(violations)
            .descriptionsAllEqualTo(PathParameterRule.ERROR_MESSAGE)
            .pointersEqualTo("/paths/~1items~1{item-id}/get/parameters/0")
    }

    @Test
    fun `return no violations if a path parameter marked as "required"`() {
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
        val violations = rule.validate(context)
        assertThat(violations).isEmpty()
    }

    @Test
    internal fun `return violation if a path parameter in components is not marked as "required"`() {
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

        val violations = rule.validate(context)

        assertThat(violations)
            .descriptionsAllEqualTo(PathParameterRule.ERROR_MESSAGE)
            .pointersEqualTo("/components/parameters/QueryParameter")
    }
}
