package de.zalando.zally.rule.zally

import com.typesafe.config.Config
import de.zalando.zally.rule.CaseChecker
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation

@Rule(
    ruleSet = ZallyRuleSet::class,
    id = "M010",
    severity = Severity.MUST,
    title = "Check case of various terms"
)
class CaseCheckerRule(config: Config) {

    private val checker = CaseChecker.load(config)

    /**
     * Check that property names match the configured requirements.
     * @param context The specification context to check.
     * @return a list of Violations, possibly empty.
     */
    @Check(severity = Severity.MUST)
    fun checkPropertyNames(context: Context): List<Violation> =
        checker.checkPropertyNames(context)

    /**
     * Check that path parameter names match the configured requirements.
     * @param context The specification context to check.
     * @return a list of Violations, possibly empty.
     */
    @Check(severity = Severity.MUST)
    fun checkPathParameterNames(context: Context): List<Violation> =
        checker.checkPathParameterNames(context)

    /**
     * Check that query parameter names match the configured requirements.
     * @param context The specification context to check.
     * @return a list of Violations, possibly empty.
     */
    @Check(severity = Severity.MUST)
    fun checkQueryParameterNames(context: Context): List<Violation> =
        checker.checkQueryParameterNames(context)

    /**
     * Check that header names match the configured requirements.
     * @param context The specification context to check.
     * @return a list of Violations, possibly empty.
     */
    @Check(severity = Severity.MUST)
    fun checkHeaderNames(context: Context): List<Violation> =
        checker.checkHeadersNames(context)

    /**
     * Check that path segments match the configured requirements.
     * @param context The specification context to check.
     * @return a list of Violations, possibly empty.
     */
    @Check(severity = Severity.MUST)
    fun checkPathSegments(context: Context): List<Violation> =
        checker.checkPathSegments(context)

    /**
     * Check that discriminator values match the configured requirements.
     * @param context The specification context to check.
     * @return a list of Violations, possibly empty.
     */
    @Check(severity = Severity.MUST)
    fun checkDiscriminatorValues(context: Context): List<Violation> =
        checker.checkDiscriminatorValues(context)

    /**
     * Check that enum values match the configured requirements.
     * @param context The specification context to check.
     * @return a list of Violations, possibly empty.
     */
    @Check(severity = Severity.MUST)
    fun checkEnumValues(context: Context): List<Violation> =
        checker.checkEnumValues(context)
}
