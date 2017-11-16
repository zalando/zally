package de.zalando.zally.rule.zalando

import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.Violation
import io.swagger.models.Swagger
import io.swagger.models.auth.ApiKeyAuthDefinition
import io.swagger.models.auth.BasicAuthDefinition
import io.swagger.models.auth.OAuth2Definition
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class UsePasswordFlowWithOauth2RuleTest {

    private val rule = DefineOAuthScopesRule(ZalandoRuleSet())

    val expectedViolation = Violation(
            rule,
            "Set Flow to Password When Using OAuth 2.0",
            "OAuth2 security definitions should use password flow",
            ViolationType.SHOULD,
            rule.url,
            emptyList())

    @Test
    fun shouldReturnNoViolationsWhenNoOauth2Found() {
        val swagger = Swagger().apply {
            securityDefinitions = mapOf(
                    "Basic" to BasicAuthDefinition(),
                    "ApiKey" to ApiKeyAuthDefinition()
            )
        }
        assertThat(rule.usePasswordFlowWithOAuth2(swagger)).isNull()
    }

    @Test
    fun shouldReturnNoViolationsWhenOauth2DefinitionsHasProperFlow() {
        val swagger = Swagger().apply {
            securityDefinitions = mapOf(
                    "Basic" to BasicAuthDefinition(),
                    "Oauth2" to OAuth2Definition().apply {
                        flow = "password"
                    }
            )
        }
        assertThat(rule.usePasswordFlowWithOAuth2(swagger)).isNull()
    }

    @Test
    fun shouldReturnViolationsWhenOauth2DefinitionsHasWrongFlow() {
        val swagger = Swagger().apply {
            securityDefinitions = mapOf(
                    "Basic" to BasicAuthDefinition(),
                    "Oauth2" to OAuth2Definition().apply {
                        flow = "implicit"
                    }
            )
        }
        assertThat(rule.usePasswordFlowWithOAuth2(swagger)).isEqualTo(expectedViolation)
    }

    @Test
    fun shouldReturnViolationsWhenOauth2DefinitionsHasNoFlow() {
        val swagger = Swagger().apply {
            securityDefinitions = mapOf(
                    "Basic" to BasicAuthDefinition(),
                    "Oauth2" to OAuth2Definition()
            )
        }
        assertThat(rule.usePasswordFlowWithOAuth2(swagger)).isEqualTo(expectedViolation)
    }

    @Test
    fun shouldReturnViolationsWhenOneOfOauth2DefinitionsIsWrong() {
        val swagger = Swagger().apply {
            securityDefinitions = mapOf(
                    "Oauth2A" to OAuth2Definition(),
                    "Oauth2B" to OAuth2Definition().apply {
                        flow = "password"
                    }
            )
        }
        assertThat(rule.usePasswordFlowWithOAuth2(swagger)).isEqualTo(expectedViolation)
    }
}
