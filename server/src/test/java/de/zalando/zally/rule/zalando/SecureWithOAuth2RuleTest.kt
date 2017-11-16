package de.zalando.zally.rule.zalando

import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.Violation
import io.swagger.models.Scheme
import io.swagger.models.Swagger
import io.swagger.models.auth.ApiKeyAuthDefinition
import io.swagger.models.auth.BasicAuthDefinition
import io.swagger.models.auth.OAuth2Definition
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SecureWithOAuth2RuleTest {

    private val rule = DefineOAuthScopesRule(ZalandoRuleSet())

    val expectedOauthViolation = Violation(
            rule,
            "Secure Endpoints with OAuth 2.0",
            "No OAuth2 security definitions found",
            ViolationType.MUST,
            rule.url,
            emptyList())

    val expectedHttpsViolation = Violation(
            rule,
            "Secure Endpoints with OAuth 2.0",
            "OAuth2 should be only used together with https",
            ViolationType.MUST,
            rule.url,
            emptyList())

    @Test
    fun emptySwagger() {
        assertThat(rule.secureWithOAuth2(Swagger())).isEqualTo(expectedOauthViolation)
    }

    @Test
    fun emptySecurityDefs() {
        val swagger = Swagger().apply {
            securityDefinitions = emptyMap()
        }
        assertThat(rule.secureWithOAuth2(swagger)).isEqualTo(expectedOauthViolation)
    }

    @Test
    fun noOAuthSecurityDef() {
        val swagger = Swagger().apply {
            securityDefinitions = mapOf(
                "Basic" to BasicAuthDefinition(),
                "ApiKey" to ApiKeyAuthDefinition()
            )
        }
        assertThat(rule.secureWithOAuth2(swagger)).isEqualTo(expectedOauthViolation)
    }

    @Test
    fun usesHttpScheme() {
        val swagger = Swagger().apply {
            schemes = listOf(Scheme.HTTP, Scheme.HTTPS)
            securityDefinitions = mapOf(
                    "Oauth2" to OAuth2Definition()
            )
        }
        assertThat(rule.secureWithOAuth2(swagger)).isEqualTo(expectedHttpsViolation)
    }

    @Test
    fun positiveCase() {
        val swagger = Swagger().apply {
            schemes = listOf(Scheme.HTTPS)
            securityDefinitions = mapOf(
                "Basic" to BasicAuthDefinition(),
                "Oauth2" to OAuth2Definition()
            )
        }
        assertThat(rule.secureWithOAuth2(swagger)).isNull()
    }
}
