package de.zalando.zally.rule.zalando

import de.zalando.zally.getFixture
import de.zalando.zally.testConfig
import io.swagger.models.Swagger
import io.swagger.parser.SwaggerParser
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

/**
 * Tests for SecureAllEndpointsWithScopesRule
 */
@Suppress("StringLiteralDuplication", "UndocumentedPublicFunction", "UnsafeCallOnNullableType", "TooManyFunctions")
class SecureAllEndpointsWithScopesRuleTest {

    private val rule = SecureAllEndpointsWithScopesRule(testConfig)

    @Test
    fun checkDefinedScopesWithNoSecurityReturnsNull() {
        val text = """
            swagger: 2.0
            """.trimIndent()
        val swagger = SwaggerParser().parse(text)

        val violation = rule.checkDefinedScopeFormats(swagger)

        assertThat(violation)
                .isNull()
    }

    @Test
    fun checkDefinedScopeFormatsWithMatchingScopesReturnsNull() {
        val text = """
            swagger: 2.0
            securityDefinitions:
              stups:
                type: oauth2
                scopes:
                  uid: Any logged in user
                  fulfillment-order.read: Can read fulfillment-order app
                  sales-order.shipment-order.write: Can create shipment-orders in the sales-order app
            """.trimIndent()
        val swagger = SwaggerParser().parse(text)

        val violation = rule.checkDefinedScopeFormats(swagger)

        assertThat(violation)
                .isNull()
    }

    @Test
    fun checkDefinedScopeFormatsWithBasicScopesReturnsNull() {
        val text = """
            swagger: 2.0
            securityDefinitions:
              lazy-in-house-scripts:
                type: basic
                scopes:
                  indexer: Can perform nightly indexing operations
                  expiry: Can perform automated expiry operations
            """.trimIndent()
        val swagger = SwaggerParser().parse(text)

        val violation = rule.checkDefinedScopeFormats(swagger)

        assertThat(violation)
                .isNull()
    }

    @Test
    fun checkDefinedScopeFormatsWithNoMatchingScopesReturnsViolation() {
        val text = """
            swagger: 2.0
            securityDefinitions:
              stups:
                type: oauth2
                scopes:
                  max: Any user called Max
            """.trimIndent()
        val swagger = SwaggerParser().parse(text)

        val violation = rule.checkDefinedScopeFormats(swagger)

        assertThat(violation!!.paths)
                .hasSameElementsAs(listOf("securityDefinitions stups max: scope 'max' does not match regex '^(uid)|(([a-z-]+\\\\.){1,2}(read|write))\$'"))
    }

    @Test
    fun checkOperationsAreScopedWithEmpty() {
        assertThat(rule.checkOperationsAreScoped(Swagger())).isNull()
    }

    @Test
    fun checkOperationsAreScopedWithoutScope() {
        val swagger = getFixture("api_without_scopes_defined.yaml")
        assertThat(rule.checkOperationsAreScoped(swagger)!!.paths).hasSize(4)
    }

    @Test
    fun checkOperationsAreScopedWithDefinedScope() {
        val swagger = getFixture("api_with_defined_scope.yaml")
        assertThat(rule.checkOperationsAreScoped(swagger)).isNull()
    }

    @Test
    fun checkOperationsAreScopedWithUndefinedScope() {
        val swagger = getFixture("api_with_undefined_scope.yaml")
        assertThat(rule.checkOperationsAreScoped(swagger)!!.paths).hasSize(2)
    }

    @Test
    fun checkOperationsAreScopedWithDefinedTopLevelScope() {
        val swagger = getFixture("api_with_toplevel_scope.yaml")
        assertThat(rule.checkOperationsAreScoped(swagger)).isNull()
    }
}
