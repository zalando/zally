# Zally Concepts

## Rule Set

A Rule Set is a bundle or a package of rules. Usually, a Rule Set
represents a logical grouping of rules (e.g. a company's REST guidelines and/or
compliance requirements).

*Example*: "Zalando Rule Set" which verifies API's compliance to
[Zalando's API Guidelines](https://opensource.zalando.com/restful-api-guidelines/).

*Implementation*: [`package de.zalando.zally.rule.zalando`](../server/src/main/java/de/zalando/zally/rule/zalando)

## Rule

A Rule is an abstract entity, it refers to a entry of the guidelines which can be
automatically checked.

*Example*: "Use standardized HTTP response codes"

*Implementation*: [`UseStandardHttpStatusCodesRule.kt`](../server/src/main/java/de/zalando/zally/rule/zalando/UseStandardHttpStatusCodesRule.kt)

```Kotlin
@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "150",
    severity = Severity.MUST,
    title = "Use Standard HTTP Status Codes"
)
class UseStandardHttpStatusCodesRule(rulesConfig: Config) { }
```

## Check

A Check implements one aspect of a Rule. Rule can contain one or multiple Checks.

*Example*: "Only standardized response codes are used"

*Implementation*:

```Kotlin
/**
 * Validate that only standardized HTTP response codes are used
 * @param context the context to validate
 * @return list of identified violations
 */
@Check(severity = Severity.MUST)
fun checkIfOnlyStandardizedResponseCodesAreUsed(context: Context): List<Violation> =
    context.validateOperations { (_, operation) ->
        operation?.responses.orEmpty().filterNot { (status, _) ->
            status in standardResponseCodes
        }.map { (status, response) ->
            context.violation("$status is not a standardized response code", response)
        }
    }
```

## Linting

Linting is the function of checking an API, i.e. running all configured Checks against the
given API specification. Output of linting is a set of violations. Each violation refers to
a Check and includes additional information like: rule, pointer, description, etc.