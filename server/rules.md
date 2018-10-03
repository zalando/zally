# Zally Rule Sets

Zally comes with a number of Rule Sets which can be selected and used. The built in rule sets are documented below.

# ZalandoRuleSet

Primarily Zally exists to enforce the various guidelines of the [Zalando RESTful API and Event Scheme Guidelines](http://zalando.github.io/restful-api-guidelines/). Individual rules descriptions won't be repeated here.

# ZallyRuleSet

Zally also contains some additional rules enforcing aspects of the OpenAPI spec or other common sense rules that don't form part of the Zalando guidelines. Those addiitonal rules are documented here.

## M008: Host should not contain protocol

Information about protocol should be placed in schema and not as part of the host.

## M009: At most one body parameter

Enforces that "there can only be one body parameter" per operation as required by the [swagger spec](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md#parameter-object).

## M010: Check case of various terms

Enforced that various terms match case requirements configured via
CaseChecker section in rules-config.conf.

Supports:

- schema property names

## S005: Do not leave unused definitions

Unused definitions cause confusion and should be avioded.

## H001: Base path can be extracted

If all paths start with the same prefix then it would be cleaner to extract that into the basePath rather than repeating for each path.

## H002: Avoid `x-zally-ignore`

The `x-zally-ignore` extension should be used sparingly for temporary exceptional circumstances. Use encourages deviation from agreed standards. For longer term solutions please discuss disabling or adjusting the rule with your team.

## 131: Use Hyphenated HTTP Headers

Header names should be hyphenated rather than use underscores or other separators
