package de.zalando.zally.rule;

import de.zalando.zally.dto.RuleDTO;
import de.zalando.zally.dto.RulesListDTO;
import de.zalando.zally.dto.SeverityBinder;
import de.zalando.zally.rule.api.Rule;
import de.zalando.zally.rule.api.Severity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@CrossOrigin
@RestController
public class SupportedRulesController {

    private final List<Rule> rules;
    private final RulesPolicy rulesPolicy;

    @Autowired
    public SupportedRulesController(List<Rule> rules, RulesPolicy rulesPolicy) {
        this.rules = rules;
        this.rulesPolicy = rulesPolicy;
    }

    @InitBinder
    public void initBinder(final WebDataBinder binder) {
        binder.registerCustomEditor(Severity.class, new SeverityBinder());
    }

    @ResponseBody
    @GetMapping("/supported-rules")
    public RulesListDTO listSupportedRules(
        @RequestParam(value = "type", required = false) Severity typeFilter,
        @RequestParam(value = "is_active", required = false) Boolean isActiveFilter) {

        List<RuleDTO> filteredRules = rules
            .stream()
            .filter(r -> filterByIsActive(r, isActiveFilter))
            .filter(r -> filterByType(r, typeFilter))
            .map(this::toDto)
            .sorted(comparing(RuleDTO::getType)
                .thenComparing(RuleDTO::getCode)
                .thenComparing(RuleDTO::getTitle))
            .collect(toList());

        return new RulesListDTO(filteredRules);
    }

    private boolean filterByIsActive(Rule rule, Boolean isActiveFilter) {
        boolean isActive = rulesPolicy.accepts(rule);
        return isActiveFilter == null || isActive == isActiveFilter;
    }

    private boolean filterByType(Rule rule, Severity typeFilter) {
        return typeFilter == null || typeFilter.equals(rule.getSeverity());
    }

    private RuleDTO toDto(Rule rule) {
        return new RuleDTO(
                rule.getTitle(),
                rule.getSeverity(),
                rule.getRuleSet().url(rule).toString(),
                rule.getId(),
                rulesPolicy.accepts(rule)
        );
    }
}
