package de.zalando.zally.rule.zalando

import de.zalando.zally.dto.ViolationType
import de.zalando.zally.getFixture
import de.zalando.zally.rule.Violation
import io.swagger.models.Scheme
import io.swagger.models.Swagger
import io.swagger.models.auth.ApiKeyAuthDefinition
import io.swagger.models.auth.BasicAuthDefinition
import io.swagger.models.auth.OAuth2Definition
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SecureWithOAuth2RuleTest {

    private val rule = SecureWithOAuth2Rule(ZalandoRuleSet())

    private val checkSecurityDefinitionsExpectedOauthViolation = Violation(
            rule,
            "Secure Endpoints with OAuth 2.0",
            "No OAuth2 security definitions found",
            ViolationType.MUST,
            emptyList())

    private val checkSecurityDefinitionsExpectedHttpsViolation = Violation(
            rule,
            "Secure Endpoints with OAuth 2.0",
            "OAuth2 should be only used together with https",
            ViolationType.MUST,
            emptyList())

    private val checkPasswordFlowExpectedViolation = Violation(
            rule,
            "Set Flow to Password When Using OAuth 2.0",
            "OAuth2 security definitions should use password flow",
            ViolationType.SHOULD,
            emptyList())

    @Test
    fun checkSecurityDefinitionsWithEmptyReturnsViolation() {
        assertThat(rule.checkSecurityDefinitions(Swagger())).isEqualTo(checkSecurityDefinitionsExpectedOauthViolation)
    }

    @Test
    fun checkSecurityDefinitionsWithEmptyDefinitionReturnsViolation() {
        val swagger = Swagger().apply {
            securityDefinitions = emptyMap()
        }
        assertThat(rule.checkSecurityDefinitions(swagger)).isEqualTo(checkSecurityDefinitionsExpectedOauthViolation)
    }

    @Test
    fun checkSecurityDefinitionsWithNoOAuth2ReturnsViolation() {
        val swagger = Swagger().apply {
            securityDefinitions = mapOf(
                "Basic" to BasicAuthDefinition(),
                "ApiKey" to ApiKeyAuthDefinition()
            )
        }
        assertThat(rule.checkSecurityDefinitions(swagger)).isEqualTo(checkSecurityDefinitionsExpectedOauthViolation)
    }

    @Test
    fun checkSecurityDefinitionsWithHttpReturnsViolation() {
        val swagger = Swagger().apply {
            schemes = listOf(Scheme.HTTP, Scheme.HTTPS)
            securityDefinitions = mapOf(
                    "Oauth2" to OAuth2Definition()
            )
        }
        assertThat(rule.checkSecurityDefinitions(swagger)).isEqualTo(checkSecurityDefinitionsExpectedHttpsViolation)
    }

    @Test
    fun checkSecurityDefinitionsWIthHttpsReturnsNothing() {
        val swagger = Swagger().apply {
            schemes = listOf(Scheme.HTTPS)
            securityDefinitions = mapOf(
                "Basic" to BasicAuthDefinition(),
                "Oauth2" to OAuth2Definition()
            )
        }
        assertThat(rule.checkSecurityDefinitions(swagger)).isNull()
    }

    @Test
    fun checkUsedScopesWithEmpty() {
        assertThat(rule.checkUsedScopes(Swagger())).isNull()
    }

    @Test
    fun checkUsedScopesWithoutScope() {
        val swagger = getFixture("api_without_scopes_defined.yaml")
        assertThat(rule.checkUsedScopes(swagger)!!.paths).hasSize(4)
    }

    @Test
    fun checkUsedScopesWithDefinedScope() {
        val swagger = getFixture("api_with_defined_scope.yaml")
        assertThat(rule.checkUsedScopes(swagger)).isNull()
    }

    @Test
    fun checkUsedScopesWithUndefinedScope() {
        val swagger = getFixture("api_with_undefined_scope.yaml")
        assertThat(rule.checkUsedScopes(swagger)!!.paths).hasSize(2)
    }

    @Test
    fun checkUsedScopesWithDefinedAndUndefinedScope() {
        val swagger = getFixture("api_with_defined_and_undefined_scope.yaml")
        assertThat(rule.checkUsedScopes(swagger)!!.paths).hasSize(2)
    }

    @Test
    fun checkUsedScopesWithDefinedTopLevelScope() {
        val swagger = getFixture("api_with_toplevel_scope.yaml")
        assertThat(rule.checkUsedScopes(swagger)).isNull()
    }

    @Test
    fun checkPasswordFlowShouldReturnNoViolationsWhenNoOauth2Found() {
        val swagger = Swagger().apply {
            securityDefinitions = mapOf(
                    "Basic" to BasicAuthDefinition(),
                    "ApiKey" to ApiKeyAuthDefinition()
            )
        }
        assertThat(rule.checkPasswordFlow(swagger)).isNull()
    }

    @Test
    fun checkPasswordFlowShouldReturnNoViolationsWhenOauth2DefinitionsHasProperFlow() {
        val swagger = Swagger().apply {
            securityDefinitions = mapOf(
                    "Basic" to BasicAuthDefinition(),
                    "Oauth2" to OAuth2Definition().apply {
                        flow = "password"
                    }
            )
        }
        assertThat(rule.checkPasswordFlow(swagger)).isNull()
    }

    @Test
    fun checkPasswordFlowShouldReturnViolationsWhenOauth2DefinitionsHasWrongFlow() {
        val swagger = Swagger().apply {
            securityDefinitions = mapOf(
                    "Basic" to BasicAuthDefinition(),
                    "Oauth2" to OAuth2Definition().apply {
                        flow = "implicit"
                    }
            )
        }
        assertThat(rule.checkPasswordFlow(swagger)).isEqualTo(checkPasswordFlowExpectedViolation)
    }

    @Test
    fun checkPasswordFlowShouldReturnViolationsWhenOauth2DefinitionsHasNoFlow() {
        val swagger = Swagger().apply {
            securityDefinitions = mapOf(
                    "Basic" to BasicAuthDefinition(),
                    "Oauth2" to OAuth2Definition()
            )
        }
        assertThat(rule.checkPasswordFlow(swagger)).isEqualTo(checkPasswordFlowExpectedViolation)
    }

    @Test
    fun checkPasswordFlowShouldReturnViolationsWhenOneOfOauth2DefinitionsIsWrong() {
        val swagger = Swagger().apply {
            securityDefinitions = mapOf(
                    "Oauth2A" to OAuth2Definition(),
                    "Oauth2B" to OAuth2Definition().apply {
                        flow = "password"
                    }
            )
        }
        assertThat(rule.checkPasswordFlow(swagger)).isEqualTo(checkPasswordFlowExpectedViolation)
    }
}
