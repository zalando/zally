package de.zalando.zally.ruleset.zalando

import de.zalando.zally.core.DefaultContextFactory
import de.zalando.zally.ruleset.zalando.util.getConfigFromContent
import de.zalando.zally.test.ZallyAssertions
import org.intellij.lang.annotations.Language
import org.junit.Test

/**
 * Tests for SecureAllEndpointsWithScopesRule
 */
@Suppress("StringLiteralDuplication", "UndocumentedPublicFunction", "UnsafeCallOnNullableType", "TooManyFunctions")
class SecureAllEndpointsWithScopesRuleTest {

    private val config = getConfigFromContent(
        """
        SecureAllEndpointsWithScopesRule {
          scope_regex: "^(uid)|(([a-z-]+\\.){1,2}(read|write))${'$'}"
          path_whitelist: [
            "^/whitelisted/.*",
            /obscure/
          ]
        }
    """.trimIndent()
    )

    private val rule = SecureAllEndpointsWithScopesRule(config)

    @Test
    fun `checkDefinedScopeFormats with no security`() {
        @Language("YAML")
        val yaml = """
            swagger: 2.0
            """.trimIndent()

        val context = DefaultContextFactory().getSwaggerContext(yaml)

        val violations = rule.checkDefinedScopeFormats(context)

        ZallyAssertions.assertThat(violations).isEmpty()
    }

    @Test
    fun `checkDefinedScopeFormats with valid oauth2 scopes`() {
        @Language("YAML")
        val yaml = """
            swagger: 2.0
            securityDefinitions:
              implicit-oauth2:
                type: oauth2
                scopes:
                  uid: Any logged in user
                  fulfillment-order.read: Can read fulfillment-order app
                  sales-order.shipment-order.write: Can create shipment-orders in the sales-order app
            """.trimIndent()

        val context = DefaultContextFactory().getSwaggerContext(yaml)

        val violations = rule.checkDefinedScopeFormats(context)

        ZallyAssertions.assertThat(violations).isEmpty()
    }

    @Test
    fun `checkDefinedScopeFormats with basic scopes`() {
        @Language("YAML")
        val yaml = """
            swagger: 2.0
            securityDefinitions:
              lazy-in-house-scripts:
                type: basic
                scopes:
                  indexer: Can perform nightly indexing operations
                  expiry: Can perform automated expiry operations
            """.trimIndent()

        val context = DefaultContextFactory().getSwaggerContext(yaml)

        val violations = rule.checkDefinedScopeFormats(context)

        ZallyAssertions.assertThat(violations).isEmpty()
    }

    @Test
    fun `checkDefinedScopeFormats with invalid oauth2 scopes`() {
        @Language("YAML")
        val yaml = """
            swagger: 2.0
            securityDefinitions:
              implicit-oauth2:
                type: oauth2
                flow: implicit
                scopes:
                  max: Any user called Max
            """.trimIndent()

        val context = DefaultContextFactory().getSwaggerContext(yaml)

        val violations = rule.checkDefinedScopeFormats(context)

        ZallyAssertions.assertThat(violations)
            .descriptionsAllEqualTo("scope 'max' does not match regex '^(uid)|(([a-z-]+\\.){1,2}(read|write))\$'")
            .pointersEqualTo("/securityDefinitions/implicit-oauth2/scopes")
    }

    @Test
    fun `checkOperationsAreScoped with empty swagger`() {
        @Language("YAML")
        val yaml = """
            swagger: 2.0
            """.trimIndent()

        val context = DefaultContextFactory().getSwaggerContext(yaml)

        val violations = rule.checkOperationsAreScoped(context)

        ZallyAssertions.assertThat(violations).isEmpty()
    }

    @Test
    fun `checkOperationsAreScoped with no scope`() {
        @Language("YAML")
        val yaml = """
            swagger: "2.0"
            securityDefinitions:
              oauth2:
                type: oauth2
                flow: password
                scopes:
                  defined-scope: A defined scope
            paths:
              /things:
                get:
                  responses:
                    200:
                      description: Success
            """.trimIndent()

        val context = DefaultContextFactory().getSwaggerContext(yaml)

        val violations = rule.checkOperationsAreScoped(context)

        ZallyAssertions.assertThat(violations)
            .descriptionsEqualTo("Endpoint not secured by OAuth2 scope(s)")
            .pointersEqualTo("/paths/~1things/get")
    }

    @Test
    fun `checkOperationsAreScoped with defined scope`() {
        @Language("YAML")
        val yaml = """
            swagger: "2.0"
            securityDefinitions:
              oauth2:
                type: oauth2
                flow: password
                scopes:
                  defined-scope: A defined scope
            paths:
              /things:
                get:
                  responses:
                    200:
                      description: Success
                  security:
                  - oauth2:
                    - defined-scope
            """.trimIndent()

        val context = DefaultContextFactory().getSwaggerContext(yaml)

        val violations = rule.checkOperationsAreScoped(context)

        ZallyAssertions.assertThat(violations).isEmpty()
    }

    @Test
    fun `checkOperationsAreScoped with undefined scope`() {
        @Language("YAML")
        val yaml = """
            swagger: "2.0"
            securityDefinitions:
              oauth2:
                type: oauth2
                flow: password
                scopes:
                  defined-scope: A defined scope
            paths:
              /things:
                get:
                  responses:
                    200:
                      description: Success
                  security:
                  - oauth2:
                    - undefined-scope
            """.trimIndent()

        val context = DefaultContextFactory().getSwaggerContext(yaml)

        val violations = rule.checkOperationsAreScoped(context)

        ZallyAssertions.assertThat(violations)
            .descriptionsEqualTo("Endpoint secured by undefined OAuth2 scope(s): oauth2:undefined-scope")
            .pointersEqualTo("/paths/~1things/get/security")
    }

    @Test
    fun `checkOperationsAreScoped with defined top level scope`() {
        @Language("YAML")
        val yaml = """
            swagger: "2.0"
            securityDefinitions:
              oauth2:
                type: oauth2
                flow: password
                scopes:
                  defined-scope: A defined scope
            security:
              - oauth2:
                - defined-scope
            paths:
              /things:
                get:
                  responses:
                    200:
                      description: Success
            """.trimIndent()

        val context = DefaultContextFactory().getSwaggerContext(yaml)

        val violations = rule.checkOperationsAreScoped(context)

        ZallyAssertions.assertThat(violations).isEmpty()
    }

    @Test
    fun `checkOperationsAreScoped with no scope on whitelisted path`() {
        @Language("YAML")
        val yaml = """
            swagger: "2.0"
            securityDefinitions:
              oauth2:
                type: oauth2
                flow: password
                scopes:
                  defined-scope: A defined scope
            paths:
              /whitelisted/path:
                get:
                  responses:
                    200:
                      description: Success
              /really/long/and/obscure/secret/path:
                get:
                  responses:
                    200:
                      description: Success
            """.trimIndent()

        val context = DefaultContextFactory().getSwaggerContext(yaml)

        val violations = rule.checkOperationsAreScoped(context)

        ZallyAssertions.assertThat(violations).isEmpty()
    }

    @Test
    fun `checkOperationsAreScoped with no scopes defined with OpenAPI components`() {
        @Language("YAML")
        val yaml = """
            openapi: 3.0.1
            
            paths:
              '/things':
                get:
                  responses:
                    200:
                      description: OK
            
            components:      
              securitySchemes:
                oauth2:
                  type: oauth2
                  flows:
                    clientCredentials:
                      tokenUrl: 'https://example.com'
            """.trimIndent()

        val context = DefaultContextFactory().getOpenApiContext(yaml)

        val violations = rule.checkOperationsAreScoped(context)

        ZallyAssertions.assertThat(violations)
            .descriptionsEqualTo("Endpoint not secured by OAuth2 scope(s)")
            .pointersEqualTo("/paths/~1things/get")
    }
}
