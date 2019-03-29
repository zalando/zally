package de.zalando.zally.integration.mock

import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn

@SpringBootConfiguration
@Configuration
class EmbeddedPostgresqlConfiguration {

    @DependsOn("postgresqlMock")
    @Bean
    fun migrationStrategy(): FlywayMigrationStrategy {
        return FlywayMigrationStrategy { it.migrate() }
    }

    @Bean
    fun postgresqlMock(): PostgresqlMock {
        return PostgresqlMock()
    }
}