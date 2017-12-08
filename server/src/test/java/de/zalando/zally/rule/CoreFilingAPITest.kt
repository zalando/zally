package de.zalando.zally.rule

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.net.URI

@RunWith(SpringRunner::class)
@SpringBootTest
@ActiveProfiles("test")
class CoreFilingAPITest {

    @Autowired
    lateinit var policy: RulesPolicy

    @Autowired
    lateinit var validator: CompositeRulesValidator

    @Test
    fun `document-service`() {
        val results = validate("platform", "document-service",
                policy.withMoreIgnores(listOf(
                        "CollectionsReturnTotalItemsHeader",
                        "PaginatedCollectionsReturnTotalPagesHeader",
                        "PaginatedCollectionsSupportPageSizeQueryParameter",
                        "PaginatedCollectionsSupportPageNumberQueryParameter",
                        "SlashesAtEnd")))

        assertThat(results).isEmpty()
    }

    @Test
    fun `commenting-service`() {
        val results = validate("platform", "commenting-service",
                policy.withMoreIgnores(listOf(
                        "PaginatedCollectionsReturnTotalPagesHeader")))

        assertThat(results).isEmpty()
    }

    @Test
    fun `filing-version-commenting-service`() {
        val results = validate("platform", "filing-version-commenting-service",
                policy.withMoreIgnores(listOf(
                        "PaginatedCollectionsReturnTotalPagesHeader")))

        assertThat(results).isEmpty()
    }

    @Test
    fun `instance-service`() {
        val results = validate("platform", "instance-service",
                policy.withMoreIgnores(listOf(
                        "CollectionsReturnTotalItemsHeader",
                        "PaginatedCollectionsReturnTotalPagesHeader",
                        "PaginatedCollectionsSupportPageNumberQueryParameter",
                        "PaginatedCollectionsSupportPageSizeQueryParameter",
                        "SlashesAtEnd")))

        assertThat(results).isEmpty()
    }

    @Test
    fun `ixbrl-rendering-service`() {
        val results = validate("platform", "ixbrl-rendering-service",
                policy.withMoreIgnores(listOf(
                        "CollectionsArePlural",
                        "CollectionsReturnTotalItemsHeader",
                        "PaginatedCollectionsReturnTotalPagesHeader",
                        "PaginatedCollectionsSupportPageNumberQueryParameter",
                        "PaginatedCollectionsSupportPageSizeQueryParameter",
                        "SlashesAtEnd")))

        assertThat(results).isEmpty()
    }

    @Test
    fun `table-rendering-service`() {
        val results = validate("platform", "table-rendering-service",
                policy.withMoreIgnores(listOf(
                        "CollectionsArePlural",
                        "CollectionsReturnTotalItemsHeader",
                        "PaginatedCollectionsReturnTotalPagesHeader",
                        "PaginatedCollectionsSupportPageNumberQueryParameter",
                        "PaginatedCollectionsSupportPageSizeQueryParameter",
                        "SlashesAtEnd")))

        assertThat(results).isEmpty()
    }

    @Test
    fun `validation-service`() {
        val results = validate("platform", "validation-service",
                policy.withMoreIgnores(listOf(
                        "CollectionsReturnTotalItemsHeader",
                        "PaginatedCollectionsReturnTotalPagesHeader",
                        "PaginatedCollectionsSupportPageNumberQueryParameter",
                        "PaginatedCollectionsSupportPageSizeQueryParameter",
                        "SlashesAtEnd")))

        assertThat(results).isEmpty()
    }

    @Test
    fun `beacon-link-service`() {
        val results = validate("fullbeam", "beacon-link-service-api", policy)

        assertThat(results).isEmpty()
    }

    @Test
    fun `discrepancies-service`() {
        val results = validate("fullbeam", "discrepancies-service-api",
                policy.withMoreIgnores(listOf(
                        "")))

        assertThat(results).isEmpty()
    }

    @Test
    fun `saved-search-service`() {
        val results = validate("fullbeam", "saved-search-service-api",
                policy.withMoreIgnores(listOf(
                        "CollectionsReturnTotalItemsHeader",
                        "PaginatedCollectionsReturnTotalPagesHeader",
                        "PaginatedCollectionsSupportPageNumberQueryParameter",
                        "PaginatedCollectionsSupportPageSizeQueryParameter")))

        assertThat(results).isEmpty()
    }

    @Test
    fun `table-diff-service`() {
        val results = validate("labs", "table-diff-service",
                policy.withMoreIgnores(listOf(
                        "CollectionsReturnTotalItemsHeader",
                        "PaginatedCollectionsReturnTotalPagesHeader",
                        "PaginatedCollectionsSupportPageNumberQueryParameter",
                        "PaginatedCollectionsSupportPageSizeQueryParameter",
                        "SlashesAtEnd")))

        assertThat(results).isEmpty()
    }

    @Test
    fun `taxonomy-package-service`() {
        val results = validate("labs", "taxonomy-package-service",
                policy.withMoreIgnores(listOf(
                        "CollectionsReturnTotalItemsHeader",
                        "PaginatedCollectionsReturnTotalPagesHeader",
                        "PaginatedCollectionsSupportPageNumberQueryParameter",
                        "PaginatedCollectionsSupportPageSizeQueryParameter",
                        "SlashesAtEnd")))

        assertThat(results).isEmpty()
    }

    @Test
    fun `digit-frequency-analysis-service`() {
        val results = validate("labs", "digit-frequency-analysis-service",
                policy.withMoreIgnores(listOf(
                        "")))

        assertThat(results).isEmpty()
    }

    @Test
    fun `filing-statistics-service`() {
        val results = validate("labs", "filing-statistics-service",
                policy.withMoreIgnores(listOf(
                        "CollectionsReturnTotalItemsHeader",
                        "PaginatedCollectionsReturnTotalPagesHeader",
                        "PaginatedCollectionsSupportPageNumberQueryParameter",
                        "PaginatedCollectionsSupportPageSizeQueryParameter",
                        "SlashesAtEnd")))

        assertThat(results).isEmpty()
    }

    private fun validate(group: String, project: String, policy: RulesPolicy): List<Violation> {
        val uri = URI.create("https://gitlab.int.corefiling.com/" + group + "/" + project + "/raw/develop/src/swagger.yaml")
        val text = uri.toURL().readText()
        return validator.validate(text, policy)
    }
}
