package org.zalando.zally.core

import com.typesafe.config.Config
import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.RuleSet

class RulesManager(val config: Config, val rules: List<RuleDetails>) {

    companion object {
        fun fromClassLoader(config: Config) =
            javaClass.classLoader
                .getResources("META-INF/services/${Rule::class.java.name}")
                .asSequence()
                .flatMap { it.readText().lineSequence() }
                .filter { it.isNotBlank() }
                .map { name ->
                    try {
                        Class.forName(name).getConstructor(Config::class.java).newInstance(config)
                    } catch (e: NoSuchMethodException) {
                        Class.forName(name).getConstructor().newInstance()
                    }
                }
                .let {
                    fromInstances(config, it.toList())
                }

        fun fromInstances(config: Config, instances: Iterable<Any>): RulesManager {
            val ruleSets = mutableMapOf<Class<out RuleSet>, RuleSet>()
            val details = instances
                .map { instance ->
                    val rule = instance::class.java.getAnnotation(Rule::class.java)
                    val ruleSet = ruleSets.computeIfAbsent(rule.ruleSet.java) {
                        it.getConstructor().newInstance()
                    }
                    RuleDetails(ruleSet, rule, instance)
                }

            return RulesManager(config, details)
        }
    }

    fun checks(policy: RulesPolicy): List<CheckDetails> {
        return rules(policy)
            .flatMap { details ->
                details.instance::class.java.methods.mapNotNull { method ->
                    method.getAnnotation(Check::class.java)?.let {
                        details.toCheckDetails(it, method)
                    }
                }
            }
    }

    fun rules(policy: RulesPolicy): List<RuleDetails> {
        return rules.filter { details -> policy.accepts(details.rule) }
    }

    fun size(): Int = rules.size
}
