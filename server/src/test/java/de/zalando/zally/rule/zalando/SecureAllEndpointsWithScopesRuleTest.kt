package de.zalando.zally.rule.zalando

import de.zalando.zally.getFixture
import de.zalando.zally.testConfig
import io.swagger.models.Swagger
import io.swagger.parser.SwaggerParser
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

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
                  sales-order.shipment_order.write: Can create shipment_orders in the sales-order app
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
                .hasSameElementsAs(listOf("securityDefinitions stups max: pseudo-scope 'max' should be one of [uid]"))
    }

    @Test
    fun checkDefinedScopeFormatWithValidPsuedoScopeReturnsNull() {
        val message = rule.checkDefinedScopeFormat("uid")
        assertThat(message).isNull()
    }

    @Test
    fun checkDefinedScopeFormatWithInvalidPsuedoScopeReturnsMessage() {
        val message = rule.checkDefinedScopeFormat("full")
        assertThat(message).isEqualTo("pseudo-scope 'full' should be one of [uid]")
    }

    @Test
    fun checkDefinedScopeFormatWithValidAccessTypeReturnsNull() {
        val message = rule.checkDefinedScopeFormat("stuff.read")
        assertThat(message).isNull()
    }

    @Test
    fun checkDefinedScopeFormatWithValidAccessTypeReturnsMessage() {
        val message = rule.checkDefinedScopeFormat("stuff.create")
        assertThat(message).isEqualTo("access-type 'create' should be one of [read, write]")
    }

    @Test
    fun checkDefinedScopeFormatWithManyComponentScopeReturnsMessage() {
        val message = rule.checkDefinedScopeFormat("this.that.the.other")
        assertThat(message).isEqualTo("scopes should have no more than 3 dot-separated components")
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
