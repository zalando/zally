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
    fun `bundle-service`() {
        val results = validate("platform", "bundle-service",
                // ignoring rules that historically failed for this service
                policy.withMoreIgnores(listOf(
                        "146", // LimitNumberOfResourcesRule
                        "150" // UseSpecificHttpStatusCodes
        )))

        assertEmptyResults(results)
    }

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
                        "146", // LimitNumberOfResourcesRule
                        "150", // UseSpecificHttpStatusCodes
                        "151", // NotSpecifyStandardErrorCodesRule
                        "171"  // FormatForNumbersRule
                )))

        assertEmptyResults(results)
    }

    @Test
    fun `commenting-service`() {
        val results = validate("platform", "commenting-service",
                // ignoring rules that historically failed for this service
                policy.withMoreIgnores(emptyList()))

        assertEmptyResults(results)
    }

    @Test
    fun `filing-version-commenting-service`() {
        val results = validate("platform", "filing-version-commenting-service",
                // ignoring rules that historically failed for this service
                policy.withMoreIgnores(listOf(
                        "CollectionsArePlural",
                        "MatchingSummaryAndOperationIdNames",
                        "PaginatedCollectionsReturnTotalPagesHeader")))

        assertEmptyResults(results)
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

        assertEmptyResults(results)
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

        assertEmptyResults(results)
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
                        "150", // UseSpecificHttpStatusCodes
                        "151"  // NotSpecifyStandardErrorCodesRule
                )))

        assertEmptyResults(results)
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

        assertEmptyResults(results)
    }

    @Test
    fun `beacon-link-service`() {
        val results = validate("fullbeam", "beacon-link-service-api",
                // ignoring rules that historically failed for this service
                policy.withMoreIgnores(listOf(
                        "MatchingSummaryAndOperationIdNames"
                )))

        assertEmptyResults(results)
    }

    @Test
    fun `discrepancies-service`() {
        val results = validate("fullbeam", "discrepancies-service-api",
                policy.withMoreIgnores(listOf(
                        "MatchingSummaryAndOperationIdNames",
                        "PostResponding200ConsideredSuspicious",
                        "151" // NotSpecifyStandardErrorCodesRule
                )))

        assertEmptyResults(results)
    }

    @Test
    fun `saved-search-service`() {
        val results = validate("fullbeam", "saved-search-service-api",
                // ignoring rules that historically failed for this service
                policy.withMoreIgnores(listOf(
                        "CollectionsReturnTotalItemsHeader",
                        "MatchingSummaryAndOperationIdNames",
                        "PaginatedCollectionsReturnTotalPagesHeader",
                        "PaginatedCollectionsSupportPageNumberQueryParameter",
                        "PaginatedCollectionsSupportPageSizeQueryParameter",
                        "PostResponding200ConsideredSuspicious",
                        "150", // UseSpecificHttpStatusCodes
                        "151"  // NotSpecifyStandardErrorCodesRule
                )))

        assertEmptyResults(results)
    }

    @Test
    fun `taxonomy-modelling-service`() {
        val results = validate("tms", "taxonomy-modelling-service",
                // ignoring rules that historically failed for this service
                policy.withMoreIgnores(listOf(
                        "MatchingSummaryAndOperationIdNames",
                        "150" // UseSpecificHttpStatusCodes
        )))

        assertEmptyResults(results)
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
                        "MatchingSummaryAndOperationIdNames",
                        "SlashesAtEnd",
                        "150", // UseSpecificHttpStatusCodes
                        "151"  // NotSpecifyStandardErrorCodesRule
        )))

        assertEmptyResults(results)
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
                        "MatchingSummaryAndOperationIdNames",
                        "SlashesAtEnd",
                        "151" // NotSpecifyStandardErrorCodesRule
                )))

        assertEmptyResults(results)
    }

    @Test
    fun `digit-frequency-analysis-service`() {
        val results = validate("labs", "digit-frequency-analysis-service",
                // ignoring rules that historically failed for this service
                policy.withMoreIgnores(listOf(
                        "MatchingSummaryAndOperationIdNames",
                        "151" // NotSpecifyStandardErrorCodesRule
                )))

        assertEmptyResults(results)
    }

    @Test
    fun `filing-statistics-service`() {
        val results = validate("labs", "filing-statistics-service",
                // ignoring rules that historically failed for this service
                policy.withMoreIgnores(listOf(
                        "CollectionsReturnTotalItemsHeader",
                        "MatchingSummaryAndOperationIdNames",
                        "PaginatedCollectionsReturnTotalPagesHeader",
                        "PaginatedCollectionsSupportPageNumberQueryParameter",
                        "PaginatedCollectionsSupportPageSizeQueryParameter",
                        "SlashesAtEnd",
                        "151", // NotSpecifyStandardErrorCodesRule
                        "171"  // FormatForNumbersRule
                )))

        assertEmptyResults(results)
    }

    private fun validate(group: String, project: String, policy: RulesPolicy, branch: String = "develop"): List<Result> {
        val uri = URI.create("https://gitlab.int.corefiling.com/$group/$project/raw/$branch/src/swagger.yaml")
        println(uri)

        return validator.validate(uri.toURL().readText(), policy.withMoreIgnores(listOf("TestAlwaysGiveAHintRule")))
    }

    private fun assertEmptyResults(results: List<Result>) {
        assertThat(results.map { r -> r.rule.id }).isEmpty()
    }
}
