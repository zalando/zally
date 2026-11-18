package org.zalando.zally.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.DispatcherType
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.client.RestClient

/**
 * Protects the API with OAuth2 bearer tokens that are validated against a tokeninfo endpoint
 * (`spring.oauth2.resource.tokenInfoUri`). A token needs the `uid` scope for every resource except `/health`.
 */
@Configuration
@EnableWebSecurity
@Profile("production")
class OAuthConfiguration {
    @Bean
    fun tokenInfoIntrospector(
        @Value("\${spring.oauth2.resource.tokenInfoUri}") tokenInfoUri: String,
    ): OpaqueTokenIntrospector = TokenInfoOpaqueTokenIntrospector(tokenInfoUri, RestClient.builder())

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        introspector: OpaqueTokenIntrospector,
        objectMapper: ObjectMapper,
    ): SecurityFilterChain {
        val problems = ProblemSecurityHandlers(objectMapper, BearerTokenAuthenticationEntryPoint())

        http
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it
                    // Spring Security 6 also secures the container's error dispatch. Without this every 404 or
                    // sendError response would turn into a 403, because /error is not an allowed path.
                    .dispatcherTypeMatchers(DispatcherType.ERROR)
                    .permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    .requestMatchers("/health")
                    .permitAll()
                    .requestMatchers(
                        "/metrics",
                        "/api-violations",
                        "/api-violations/*",
                        "/supported-rules",
                        "/review-statistics",
                    ).hasAuthority("SCOPE_uid")
                    .anyRequest()
                    .denyAll()
            }.oauth2ResourceServer { resourceServer ->
                resourceServer
                    .opaqueToken { it.introspector(introspector) }
                    .authenticationEntryPoint(problems)
                    .accessDeniedHandler(problems)
            }.addFilterBefore(TokenInfoOutageFilter(problems), BearerTokenAuthenticationFilter::class.java)

        return http.build()
    }
}
