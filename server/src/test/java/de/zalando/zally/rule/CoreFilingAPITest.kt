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
                // ignoring rules that historically failed for this service
                policy.withMoreIgnores(listOf(
                        "CollectionsReturnTotalItemsHeader",
                        "PaginatedCollectionsReturnTotalPagesHeader",
                        "PaginatedCollectionsSupportPageSizeQueryParameter",
                        "PaginatedCollectionsSupportPageNumberQueryParameter",
                        "SlashesAtEnd",
                        "132", // PascalCaseHttpHeadersRule
                        "146", // LimitNumberOfResourcesRule
                        "150", // UseSpecificHttpStatusCodes
                        "151", // NotSpecifyStandardErrorCodesRule
                        "171"  // FormatForNumbersRule
                )))

        assertThat(results).isEmpty()
    }

    @Test
    fun `commenting-service`() {
        val results = validate("platform", "commenting-service",
                // ignoring rules that historically failed for this service
                policy.withMoreIgnores(listOf(
                        "PaginatedCollectionsReturnTotalPagesHeader",
                        "132", // PascalCaseHttpHeadersRule
                        "150"  // UseSpecificHttpStatusCodes
                )))

        assertThat(results).isEmpty()
    }

    @Test
    fun `filing-version-commenting-service`() {
        val results = validate("platform", "filing-version-commenting-service",
                // ignoring rules that historically failed for this service
                policy.withMoreIgnores(listOf(
                        "PaginatedCollectionsReturnTotalPagesHeader")))

        assertThat(results).isEmpty()
    }

    @Test
    fun `instance-service`() {
        val results = validate("platform", "instance-service",
                // ignoring rules that historically failed for this service
                policy.withMoreIgnores(listOf(
                        "CollectionsReturnTotalItemsHeader",
                        "PaginatedCollectionsReturnTotalPagesHeader",
                        "PaginatedCollectionsSupportPageNumberQueryParameter",
                        "PaginatedCollectionsSupportPageSizeQueryParameter",
                        "SlashesAtEnd",
                        "150", // UseSpecificHttpStatusCodes
                        "151"  // NotSpecifyStandardErrorCodesRule
                )))

        assertThat(results).isEmpty()
    }

    @Test
    fun `ixbrl-rendering-service`() {
        val results = validate("platform", "ixbrl-rendering-service",
                // ignoring rules that historically failed for this service
                policy.withMoreIgnores(listOf(
                        "CollectionsArePlural",
                        "CollectionsReturnTotalItemsHeader",
                        "PaginatedCollectionsReturnTotalPagesHeader",
                        "PaginatedCollectionsSupportPageNumberQueryParameter",
                        "PaginatedCollectionsSupportPageSizeQueryParameter",
                        "SlashesAtEnd",
                        "150", // UseSpecificHttpStatusCodes
                        "151"  // NotSpecifyStandardErrorCodesRule
                )))

        assertThat(results).isEmpty()
    }

    @Test
    fun `table-rendering-service`() {
        val results = validate("platform", "table-rendering-service",
                // ignoring rules that historically failed for this service
                policy.withMoreIgnores(listOf(
                        "CollectionsArePlural",
                        "CollectionsReturnTotalItemsHeader",
                        "PaginatedCollectionsReturnTotalPagesHeader",
                        "PaginatedCollectionsSupportPageNumberQueryParameter",
                        "PaginatedCollectionsSupportPageSizeQueryParameter",
                        "SlashesAtEnd",
                        "120", // PluralizeNamesForArraysRule
                        "146", // LimitNumberOfResourcesRule
                        "150", // UseSpecificHttpStatusCodes
                        "151"  // NotSpecifyStandardErrorCodesRule
                )))

        assertThat(results).isEmpty()
    }

    @Test
    fun `validation-service`() {
        val results = validate("platform", "validation-service",
                // ignoring rules that historically failed for this service
                policy.withMoreIgnores(listOf(
                        "CollectionsReturnTotalItemsHeader",
                        "PaginatedCollectionsReturnTotalPagesHeader",
                        "PaginatedCollectionsSupportPageNumberQueryParameter",
                        "PaginatedCollectionsSupportPageSizeQueryParameter",
                        "SlashesAtEnd",
                        "150", // UseSpecificHttpStatusCodes
                        "151", // NotSpecifyStandardErrorCodesRule
                        "171"  // FormatForNumbersRule
                )))

        assertThat(results).isEmpty()
    }

    @Test
    fun `beacon-link-service`() {
        val results = validate("fullbeam", "beacon-link-service-api",
                // ignoring rules that historically failed for this service
                policy)

        assertThat(results).isEmpty()
    }

    @Test
    fun `discrepancies-service`() {
        val results = validate("fullbeam", "discrepancies-service-api",
                policy.withMoreIgnores(listOf(
                        "PostResponding200ConsideredSuspicious",
                        "151" // NotSpecifyStandardErrorCodesRule
                )))

        assertThat(results).isEmpty()
    }

    @Test
    fun `saved-search-service`() {
        val results = validate("fullbeam", "saved-search-service-api",
                // ignoring rules that historically failed for this service
                policy.withMoreIgnores(listOf(
                        "CollectionsReturnTotalItemsHeader",
                        "PaginatedCollectionsReturnTotalPagesHeader",
                        "PaginatedCollectionsSupportPageNumberQueryParameter",
                        "PaginatedCollectionsSupportPageSizeQueryParameter",
                        "PostResponding200ConsideredSuspicious",
                        "150", // UseSpecificHttpStatusCodes
                        "151"  // NotSpecifyStandardErrorCodesRule
                )))

        assertThat(results).isEmpty()
    }

    @Test
    fun `table-diff-service`() {
        val results = validate("labs", "table-diff-service",
                // ignoring rules that historically failed for this service
                policy.withMoreIgnores(listOf(
                        "CollectionsReturnTotalItemsHeader",
                        "PaginatedCollectionsReturnTotalPagesHeader",
                        "PaginatedCollectionsSupportPageNumberQueryParameter",
                        "PaginatedCollectionsSupportPageSizeQueryParameter",
                        "SlashesAtEnd",
                        "146", // LimitNumberOfResourcesRule
                        "150", // UseSpecificHttpStatusCodes
                        "151"  // NotSpecifyStandardErrorCodesRule
        )))

        assertThat(results).isEmpty()
    }

    @Test
    fun `taxonomy-package-service`() {
        val results = validate("labs", "taxonomy-package-service",
                // ignoring rules that historically failed for this service
                policy.withMoreIgnores(listOf(
                        "CollectionsReturnTotalItemsHeader",
                        "PaginatedCollectionsReturnTotalPagesHeader",
                        "PaginatedCollectionsSupportPageNumberQueryParameter",
                        "PaginatedCollectionsSupportPageSizeQueryParameter",
                        "SlashesAtEnd",
                        "151" // NotSpecifyStandardErrorCodesRule
                )))

        assertThat(results).isEmpty()
    }

    @Test
    fun `digit-frequency-analysis-service`() {
        val results = validate("labs", "digit-frequency-analysis-service",
                // ignoring rules that historically failed for this service
                policy.withMoreIgnores(listOf(
                        "151" // NotSpecifyStandardErrorCodesRule
                )))

        assertThat(results).isEmpty()
    }

    @Test
    fun `filing-statistics-service`() {
        val results = validate("labs", "filing-statistics-service",
                // ignoring rules that historically failed for this service
                policy.withMoreIgnores(listOf(
                        "CollectionsReturnTotalItemsHeader",
                        "PaginatedCollectionsReturnTotalPagesHeader",
                        "PaginatedCollectionsSupportPageNumberQueryParameter",
                        "PaginatedCollectionsSupportPageSizeQueryParameter",
                        "SlashesAtEnd",
                        "151", // NotSpecifyStandardErrorCodesRule
                        "171"  // FormatForNumbersRule
                )))

        assertThat(results).isEmpty()
    }

    private fun validate(group: String, project: String, policy: RulesPolicy): List<Violation> {
        val uri = URI.create("https://gitlab.int.corefiling.com/" + group + "/" + project + "/raw/develop/src/swagger.yaml")
        println(uri)

        val text = uri.toURL().readText()
        return validator.validate(text, policy)
    }
}
