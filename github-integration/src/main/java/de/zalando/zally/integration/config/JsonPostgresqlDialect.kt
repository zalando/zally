package de.zalando.zally.integration.config

import org.hibernate.dialect.PostgreSQL9Dialect

import java.sql.Types

class JsonPostgresqlDialect : PostgreSQL9Dialect() {
    init {
        registerColumnType(Types.JAVA_OBJECT, "jsonb")
    }
}