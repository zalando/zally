package de.zalando.zally.integration.mock

import de.zalando.zally.integration.config.logger
import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

class PostgresqlMock(private val dbName: String = "barks",
                     private val port: Int = 5454,
                     private val postgres: EmbeddedPostgres = EmbeddedPostgres()) {

    private val log by logger()

    @PostConstruct
    fun setup() {
        log.info("Starting embedded postgresql")
        postgres.start("localhost", port, dbName, "postgres", "postgres")
    }

    @PreDestroy
    fun teardown() {
        log.info("Stopping embedded postgresql")
        postgres.stop()
    }

}