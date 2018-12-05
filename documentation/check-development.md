# Rule Development Manual

## A Basic Check

In the following snippet you find a basic check implementation:

```Kotlin
package de.zalando.zally.rule.nicecompany

import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation

@Rule(
    ruleSet = NiceCompanyRuleSet::class,
    id = "001",
    severity = Severity.MUST,
    title = "Contain API Meta Information"
)
class ApiMetaInformationRule {

    @Check(severity = Severity.MUST)
    fun checkInfoTitle(context: Context): Violation? =
        if (context.api.info?.title.isNullOrBlank()) {
            context.violation("Title has to be provided")
        } else null

}

```

Check function has to be annotated with `@Check` and operate on a `Context`
object. As output it should return either an optional `Violation`,
or a `List` of `Violations`.

The check references to `NiceCompanyRuleSet`:

```Kotlin
package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.AbstractRuleSet
import de.zalando.zally.rule.api.Rule
import org.springframework.stereotype.Component
import java.net.URI

@Component
class NiceCompanyRuleSet : AbstractRuleSet() {
    override val id: String = javaClass.simpleName
    override val title: String = "Nice Company's REST Guidelines"
    override val url: URI = URI.create("https://very.nice.compan.io/restguidelines/")
    override fun url(rule: Rule): URI {
        return url.resolve("#${rule.id}")
    }
}
```

## Traverse the specification

`Context` object contains raw API specification as `String` (`Context#source`), parsed
`OpenAPI` object (`Context#openApi`), and an optional `Swagger` object (`Context#swagger`).
`OpenAPI` object represents OpenAPI 3 specification. If an OpenAPI 2 spefication (Swagger 2)
is provided, the `Context#swagger` object is not empty and represents an OpenAPI 2 document.
In both cases, the `OpenAPI` object is present and contains either a parsed, or a converted
(from Swagger 2) specification.

- [`OpenAPI` reference](https://github.com/swagger-api/swagger-core/blob/master/modules/swagger-models/src/main/java/io/swagger/v3/oas/models/OpenAPI.java)
- [`Swagger` reference](https://static.javadoc.io/io.swagger/swagger-models/1.5.9/io/swagger/models/Swagger.html)

The object structures correspond with the respective
[OpenAPI specifications](https://github.com/OAI/OpenAPI-Specification/tree/master/versions).

### Helper functions

Besides the specification objects, Zally also provides useful helper functions.
Here are some examples:

- `Context#validatePaths(pathFilter, action): List<Violation>`
- `Context#validateOperations(pathFilter, operationFilter, action): List<Violation>`

See [`DefaultContext`](../server/src/main/java/de/zalando/zally/rule/DefaultContext.kt)
for more information.

## Create a violation

In order to create a violation, you should use
`Context#violation(desc: String, value: Any?)` function. It should contain violation
description. Optionally, you can supply a leaf from the `openApi` object. This would
create a pointer to violation location in the documentation.