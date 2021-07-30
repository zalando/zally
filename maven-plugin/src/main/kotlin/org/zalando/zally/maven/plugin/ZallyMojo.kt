package org.zalando.zally.maven.plugin

import com.typesafe.config.ConfigFactory
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.zalando.zally.core.ApiValidator
import org.zalando.zally.core.CompositeRulesValidator
import org.zalando.zally.core.ContextRulesValidator
import org.zalando.zally.core.DefaultContextFactory
import org.zalando.zally.core.JsonRulesValidator
import org.zalando.zally.core.Result
import org.zalando.zally.core.RulesManager
import org.zalando.zally.core.RulesPolicy
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation
import java.io.File
import java.net.URI
import java.util.Objects
import java.util.TreeMap

@Mojo(name = "validate", defaultPhase = LifecyclePhase.VALIDATE)
class ZallyMojo : AbstractMojo() {

    /**
     * Path to a directory with OpenAPI / Swagger specifications
     */
    @Parameter(property = "zally.maven.plugin.inputDir", required = true)
    lateinit var inputDir: String

    /**
     * IDs of rules to ignore
     */
    @Parameter(property = "zally.maven.plugin.ignoredRules")
    lateinit var ignoredRules: List<String>

    override fun execute() {
        val apiValidator = createApiValidator(RulesManager.fromClassLoader(ConfigFactory.load()))
        val rulesPolicy = RulesPolicy(ignoredRules)
        var nonblockingViolationsCount = 0
        var blockingViolationsCount = 0
        val log = log
        for (file: File in getFiles(inputDir)) {
            log.info("")
            log.info("Path: " + file.path)
            val violations = apiValidator.validate(file.readText(), rulesPolicy, "")
            if (violations.isEmpty()) {
                log.info("Spec follows the rules")
                continue
            }

            log.warn("Spec breaks the rules:")
            val blockingAndNonblockingViolations =
                violations.partition { it.violationType == Severity.MUST }

            blockingAndNonblockingViolations.second.takeIf { it.isNotEmpty() }
                ?.let {
                    logViolations(it, log::warn)
                    nonblockingViolationsCount += it.size
                }

            blockingAndNonblockingViolations.first.takeIf { it.isNotEmpty() }
                ?.let {
                    logViolations(it, log::error)
                    blockingViolationsCount += it.size
                }
        }
        log.info("")
        log.info("Overall results:")
        log.info("Violations of nonmandatory rules: $nonblockingViolationsCount")
        log.info("Violations of mandatory rules: $blockingViolationsCount")
        if (blockingViolationsCount > 0) {
            throw MojoExecutionException("Validation failed!")
        }
    }

    fun createApiValidator(rulesManager: RulesManager): ApiValidator =
        CompositeRulesValidator(
            ContextRulesValidator(
                rulesManager,
                DefaultContextFactory()
            ),
            JsonRulesValidator(rulesManager)
        )

    fun getFiles(inputDir: String): Sequence<File> = File(inputDir).walk().filter { it.isFile }

    private fun logViolations(violationsToLog: List<Result>, logger: (String) -> Unit) {
        toRuleViolationsMap(violationsToLog).forEach { (rule, violations) ->
            logger("  Violations of a rule ${rule.id} (see ${rule.uri}) :")
            violations.forEach {
                logger("    - pointer: ${it.pointer}")
                logger("      description: ${it.description}")
            }
        }
    }

    private fun toRuleViolationsMap(violations: List<Result>): Map<Rule, List<Violation>> =
        violations.groupByTo(
            TreeMap(),
            { Rule(it.id, it.url) },
            { Violation(it.description, it.pointer) }
        )

    private class Rule(val id: String, val uri: URI) : Comparable<Rule> {

        override fun compareTo(other: Rule): Int = id.compareTo(other.id)

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (javaClass != other?.javaClass) {
                return false
            }
            return id == (other as Rule).id
        }

        override fun hashCode(): Int = Objects.hash(id)
    }
}
