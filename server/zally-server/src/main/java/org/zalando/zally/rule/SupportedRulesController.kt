package org.zalando.zally.rule

import org.zalando.zally.core.RuleDetails
import org.zalando.zally.core.RulesManager
import org.zalando.zally.core.RulesPolicy
import org.zalando.zally.dto.RuleDTO
import org.zalando.zally.dto.RulesListDTO
import org.zalando.zally.dto.SeverityBinder
import org.zalando.zally.rule.api.Severity
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.InitBinder
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

/**
 * REST API for listing the rules supported by this server.
 */
@CrossOrigin
@RestController
class SupportedRulesController(private val rules: RulesManager, private val rulesPolicy: RulesPolicy) {

    /**
     * Registers serialization binding for Severity enum
     * @param binder the binder to configure
     */
    @InitBinder
    fun initBinder(binder: WebDataBinder) {
        binder.registerCustomEditor(Severity::class.java, SeverityBinder())
    }

    /**
     * GET /supported-rules implementation.
     * @param typeFilter filter rules to just this severity
     * @param isActiveFilter filter rules to just active ones
     * @return rules list ready for serialization
     */
    @ResponseBody
    @GetMapping("/supported-rules")
    fun listSupportedRules(
        @RequestParam(value = "type", required = false) typeFilter: Severity?,
        @RequestParam(value = "is_active", required = false) isActiveFilter: Boolean?
    ): RulesListDTO {

        val filteredRules = rules
            .rules
            .filter { filterByIsActive(it, isActiveFilter) }
            .filter { filterByType(it, typeFilter) }
            .map { this.toDto(it) }
            .sortedWith(
                compareBy(
                    RuleDTO::type,
                    RuleDTO::code,
                    RuleDTO::title
                )
            )

        return RulesListDTO(filteredRules)
    }

    private fun filterByIsActive(details: RuleDetails, isActiveFilter: Boolean?): Boolean {
        val isActive = rulesPolicy.accepts(details.rule)
        return isActiveFilter == null || isActive == isActiveFilter
    }

    private fun filterByType(details: RuleDetails, typeFilter: Severity?): Boolean {
        return typeFilter == null || typeFilter == details.rule.severity
    }

    private fun toDto(details: RuleDetails): RuleDTO {
        return RuleDTO(
            details.rule.title,
            details.rule.severity,
            details.ruleSet.url(details.rule).toString(),
            details.rule.id,
            rulesPolicy.accepts(details.rule)
        )
    }
}
