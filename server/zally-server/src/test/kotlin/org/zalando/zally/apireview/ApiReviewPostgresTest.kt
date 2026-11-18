package org.zalando.zally.apireview

import com.fasterxml.jackson.core.JsonPointer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import org.zalando.zally.core.Result
import org.zalando.zally.dto.ApiDefinitionRequest
import org.zalando.zally.rule.api.Severity
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.TimeZone
import java.util.UUID
import jakarta.persistence.EntityManager

/**
 * Runs the real Flyway migrations and the JPA mapping against PostgreSQL 13 (the version used by
 * `docker-compose.yaml`), which every other test replaces with HSQLDB.
 *
 * It pins the storage contract that must survive framework upgrades: `api_review.created` is a
 * `TIMESTAMP WITHOUT TIME ZONE` holding UTC wall-clock time, whatever the JVM default time zone.
 */
@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ApiReviewPostgresTest {
    @Autowired
    private lateinit var repository: ApiReviewRepository

    @Autowired
    private lateinit var entityManager: EntityManager

    @Autowired
    private lateinit var jdbc: JdbcTemplate

    private val originalTimeZone = TimeZone.getDefault()

    @AfterEach
    fun restoreTimeZone() = TimeZone.setDefault(originalTimeZone)

    @Test
    fun `flyway applies all migrations`() {
        val versions =
            jdbc.queryForList(
                "SELECT version FROM flyway_schema_history WHERE success ORDER BY installed_rank",
                String::class.java,
            )

        assertThat(versions).containsExactly("001", "002", "003", "004", "005", "006", "007")
    }

    @Test
    fun `round-trips an api review with its violations`() {
        val review =
            ApiReview(
                request = ApiDefinitionRequest(),
                userAgent = "zally-cli/1.0",
                apiDefinition = OPENAPI,
                violations =
                    listOf(
                        result("R1", Severity.MUST, "/paths", 3..4),
                        result("R2", Severity.SHOULD, "/info"),
                    ),
                created = CREATED,
            )
        repository.save(review)
        flushAndClear()

        val found = repository.findByExternalId(review.externalId)!!

        assertThat(found.id).isEqualTo(review.id).isNotZero
        assertThat(found.externalId).isEqualTo(review.externalId)
        assertThat(found.userAgent).isEqualTo("zally-cli/1.0")
        assertThat(found.name).isEqualTo("Test API")
        assertThat(found.apiId).isEqualTo("test-api-id")
        assertThat(found.apiDefinition).isEqualTo(OPENAPI)
        assertThat(found.isSuccessfulProcessed).isTrue
        assertThat(found.numberOfEndpoints).isEqualTo(1)
        assertThat(found.day).isEqualTo(LocalDate.of(2023, 5, 1))
        assertThat(found.created.toInstant()).isEqualTo(CREATED.toInstant())
        assertThat(found.mustViolations).isEqualTo(1)
        assertThat(found.shouldViolations).isEqualTo(1)
        assertThat(found.mayViolations).isEqualTo(0)
        assertThat(found.hintViolations).isEqualTo(0)

        assertThat(found.ruleViolations).hasSize(2)
        val must = found.ruleViolations.single { it.type == Severity.MUST }
        assertThat(must.name).isEqualTo("Title R1 (R1)")
        assertThat(must.ruleTitle).isEqualTo("Title R1")
        assertThat(must.ruleUrl).isEqualTo("https://rules.example.com/R1")
        assertThat(must.description).isEqualTo("Description R1")
        assertThat(must.locationPointer).isEqualTo("/paths")
        assertThat(must.locationLineStart).isEqualTo(3)
        assertThat(must.locationLineEnd).isEqualTo(4)
        val should = found.ruleViolations.single { it.type == Severity.SHOULD }
        assertThat(should.locationLineStart).isNull()
        assertThat(should.locationLineEnd).isNull()
    }

    @Test
    fun `findByExternalId returns null for an unknown id`() {
        assertThat(repository.findByExternalId(UUID.randomUUID())).isNull()
    }

    @Test
    fun `externalId is stored in a native uuid column`() {
        val review = repository.save(ApiReview(ApiDefinitionRequest(), apiDefinition = OPENAPI))
        flushAndClear()

        val stored =
            jdbc.queryForObject(
                "SELECT external_id FROM api_review WHERE id = ?",
                UUID::class.java,
                review.id,
            )

        assertThat(stored).isEqualTo(review.externalId)
    }

    @Test
    fun `created is written as UTC wall-clock time`() {
        // a non-UTC offset on input: 12:15:30+02:00 is 10:15:30 UTC
        val created = OffsetDateTime.of(2023, 5, 1, 12, 15, 30, 0, ZoneOffset.ofHours(2))
        val review = repository.save(ApiReview(ApiDefinitionRequest(), apiDefinition = OPENAPI, created = created))
        flushAndClear()

        // The column has no time zone, so read the literal wall-clock value, not a Timestamp through the JVM zone.
        val literal =
            jdbc.queryForObject(
                "SELECT to_char(created, 'YYYY-MM-DD\"T\"HH24:MI:SS') FROM api_review WHERE id = ?",
                String::class.java,
                review.id,
            )

        assertThat(literal).isEqualTo("2023-05-01T10:15:30")
    }

    @ParameterizedTest
    @ValueSource(strings = ["UTC", "Europe/Berlin", "America/Los_Angeles", "Pacific/Auckland"])
    fun `created is written as UTC wall-clock time whatever the JVM time zone`(zone: String) {
        // Hibernate 5 with Jadira stored the wall-clock time of the JVM zone; since Hibernate 6 it is always UTC
        // (hibernate.jdbc.time_zone). Identical in a UTC container, which is why existing rows stay valid.
        TimeZone.setDefault(TimeZone.getTimeZone(zone))
        val created = OffsetDateTime.of(2023, 5, 1, 12, 15, 30, 0, ZoneOffset.ofHours(2))
        val review = repository.save(ApiReview(ApiDefinitionRequest(), apiDefinition = OPENAPI, created = created))
        flushAndClear()

        val literal =
            jdbc.queryForObject(
                "SELECT to_char(created, 'YYYY-MM-DD\"T\"HH24:MI:SS') FROM api_review WHERE id = ?",
                String::class.java,
                review.id,
            )

        assertThat(literal).isEqualTo("2023-05-01T10:15:30")
    }

    @ParameterizedTest
    @ValueSource(strings = ["UTC", "Europe/Berlin", "America/Los_Angeles", "Pacific/Auckland"])
    fun `reads a row written as UTC whatever the JVM time zone`(zone: String) {
        TimeZone.setDefault(TimeZone.getTimeZone(zone))
        val externalId = UUID.randomUUID()
        jdbc.update(
            """
            INSERT INTO api_review (
                name, api_definition, successful_processed, day, created, number_of_endpoints,
                must_violations, should_violations, may_violations, hint_violations, external_id
            ) VALUES ('Legacy API', '{}', TRUE, DATE '2023-05-01', TIMESTAMP '2023-05-01 10:15:30', 0, 0, 0, 0, 0, ?)
            """.trimIndent(),
            externalId,
        )
        entityManager.clear()

        val found = repository.findByExternalId(externalId)!!

        assertThat(found.created.toInstant()).isEqualTo(OffsetDateTime.of(2023, 5, 1, 10, 15, 30, 0, ZoneOffset.UTC).toInstant())
    }

    @Test
    fun `column type of created is timestamp without time zone`() {
        val type =
            jdbc.queryForObject(
                "SELECT data_type FROM information_schema.columns WHERE table_name = 'api_review' AND column_name = 'created'",
                String::class.java,
            )

        assertThat(type).isEqualTo("timestamp without time zone")
    }

    @Test
    fun `reads a row written by an earlier version as UTC`() {
        val externalId = UUID.randomUUID()
        // Exactly what the Boot 2.7 / Jadira build stores: UTC wall-clock in a TIMESTAMP WITHOUT TIME ZONE.
        jdbc.update(
            """
            INSERT INTO api_review (
                name, api_definition, successful_processed, day, created, number_of_endpoints,
                must_violations, should_violations, may_violations, hint_violations,
                user_agent, api_id, external_id
            ) VALUES (
                'Legacy API', '{}', TRUE, DATE '2023-05-01', TIMESTAMP '2023-05-01 10:15:30', 7,
                2, 1, 0, 0, 'legacy-agent', 'legacy-id', ?
            )
            """.trimIndent(),
            externalId,
        )
        entityManager.clear()

        val found = repository.findByExternalId(externalId)!!

        assertThat(found.created).isEqualTo(OffsetDateTime.of(2023, 5, 1, 10, 15, 30, 0, ZoneOffset.UTC))
        assertThat(found.day).isEqualTo(LocalDate.of(2023, 5, 1))
        assertThat(found.name).isEqualTo("Legacy API")
        assertThat(found.userAgent).isEqualTo("legacy-agent")
        assertThat(found.numberOfEndpoints).isEqualTo(7)
        assertThat(found.mustViolations).isEqualTo(2)
        assertThat(found.ruleViolations).isEmpty()
    }

    @Test
    fun `reads a legacy row together with its violations`() {
        val externalId = UUID.randomUUID()
        jdbc.update(
            """
            INSERT INTO api_review (
                name, api_definition, successful_processed, day, created, number_of_endpoints,
                must_violations, should_violations, may_violations, hint_violations, external_id
            ) VALUES ('Legacy API', '{}', TRUE, DATE '2023-05-01', TIMESTAMP '2023-05-01 10:15:30', 0, 1, 0, 0, 0, ?)
            """.trimIndent(),
            externalId,
        )
        jdbc.update(
            """
            INSERT INTO rule_violation (
                api_review_id, name, type, occurrence, description, rule_title, rule_url,
                location_pointer, location_line_start, location_line_end
            ) SELECT id, 'Legacy rule (L1)', 'MUST', 1, 'desc', 'Legacy rule', 'https://rules.example.com/L1',
                '/info', 5, 6
            FROM api_review WHERE external_id = ?
            """.trimIndent(),
            externalId,
        )
        entityManager.clear()

        val violations = repository.findByExternalId(externalId)!!.ruleViolations

        assertThat(violations).hasSize(1)
        assertThat(violations.single().type).isEqualTo(Severity.MUST)
        assertThat(violations.single().locationLineStart).isEqualTo(5)
        assertThat(violations.single().locationLineEnd).isEqualTo(6)
    }

    @Test
    fun `statistics aggregate the reviews inside the date range`() {
        save(day = "2023-05-01", agent = "cli", name = "A", must = 2, should = 1, endpoints = 3)
        save(day = "2023-05-02", agent = "cli", name = "A", must = 1, may = 4, endpoints = 3)
        save(day = "2023-05-03", agent = "web", name = "B", hint = 5, endpoints = 3)
        save(day = "2023-05-04", agent = "web", name = "C", must = 9, endpoints = 3) // outside the range
        // unparsable definition: counted as total but not as successful
        repository.save(
            ApiReview(
                ApiDefinitionRequest(),
                userAgent = "web",
                apiDefinition = "",
                created = at("2023-05-02"),
            ),
        )
        flushAndClear()

        val all = repository.getReviewStatistics(LocalDate.of(2023, 5, 1), LocalDate.of(2023, 5, 3))

        assertThat(all.totalReviews).isEqualTo(4)
        assertThat(all.successfulReviews).isEqualTo(3)
        assertThat(all.numberOfEndpoints).isEqualTo(9)
        assertThat(all.mustViolations).isEqualTo(3)
        assertThat(all.shouldViolations).isEqualTo(1)
        assertThat(all.mayViolations).isEqualTo(4)
        assertThat(all.hintViolations).isEqualTo(5)
    }

    @Test
    fun `statistics filter by user agent with LIKE semantics`() {
        save(day = "2023-05-01", agent = "zally-cli/1.0", name = "A", must = 1)
        save(day = "2023-05-01", agent = "zally-cli/2.0", name = "A", must = 2)
        save(day = "2023-05-01", agent = "browser", name = "B", must = 4)
        flushAndClear()
        val from = LocalDate.of(2023, 5, 1)

        val cli = repository.getReviewStatistics(from, from, "zally-cli%")
        val browser = repository.getReviewStatistics(from, from, "browser")

        assertThat(cli.totalReviews).isEqualTo(2)
        assertThat(cli.mustViolations).isEqualTo(3)
        assertThat(browser.totalReviews).isEqualTo(1)
        assertThat(browser.mustViolations).isEqualTo(4)
    }

    @Test
    fun `statistics for an empty range are all zero`() {
        save(day = "2023-05-01", agent = "cli", name = "A", must = 1)
        flushAndClear()

        val stats = repository.getReviewStatistics(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2))

        assertThat(stats.totalReviews).isEqualTo(0)
        assertThat(stats.successfulReviews).isEqualTo(0)
        assertThat(stats.numberOfEndpoints).isEqualTo(0)
        assertThat(stats.mustViolations).isEqualTo(0)
        assertThat(stats.hintViolations).isEqualTo(0)
    }

    private fun save(
        day: String,
        agent: String,
        name: String,
        must: Int = 0,
        should: Int = 0,
        may: Int = 0,
        hint: Int = 0,
        endpoints: Int = 1,
    ) {
        val violations =
            List(must) { result("M$it", Severity.MUST) } +
                List(should) { result("S$it", Severity.SHOULD) } +
                List(may) { result("Y$it", Severity.MAY) } +
                List(hint) { result("H$it", Severity.HINT) }
        repository.save(
            ApiReview(
                ApiDefinitionRequest(),
                userAgent = agent,
                apiDefinition = OPENAPI,
                violations = violations,
                name = name,
                created = at(day),
                numberOfEndpoints = endpoints,
            ),
        )
    }

    private fun at(day: String): OffsetDateTime = LocalDateTime.parse("${day}T12:00:00").atOffset(ZoneOffset.UTC)

    private fun flushAndClear() {
        entityManager.flush()
        entityManager.clear()
    }

    private fun result(
        id: String,
        severity: Severity,
        pointer: String = "/",
        lines: IntRange? = null,
    ) = Result(
        id = id,
        url = URI.create("https://rules.example.com/$id"),
        title = "Title $id",
        description = "Description $id",
        violationType = severity,
        pointer = JsonPointer.compile(pointer),
        lines = lines,
    )

    companion object {
        private val CREATED = OffsetDateTime.of(2023, 5, 1, 10, 15, 30, 0, ZoneOffset.UTC)

        private val OPENAPI =
            """
            {"swagger":"2.0","info":{"title":"Test API","x-api-id":"test-api-id","version":"1"},
             "paths":{"/pets":{"get":{"responses":{"200":{"description":"ok"}}}}}}
            """.trimIndent()

        @Container
        @JvmField
        val postgres = PostgreSQLContainer("postgres:13")

        @JvmStatic
        @DynamicPropertySource
        fun datasource(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }
}
