# How the SBB ruleset is created

We take the approach to start from Zalando's ruleset and then tailor it to SBB's need.

## Initial Steps
1. Copy from zally-ruleset-zalando
2. Refactor package names
3. Add directory 'zalando-ruleset-sbb' to 'settings.gradle.kts'
4. Add 'compile(project(":zally-ruleset-sbb"))' to 'build.gradke.kts'
5. Rename 'ZalandoRuleSet' to 'SBBRuleSet'

## Configuration of Rules
- Removed "Provide API Audience" (219)
- Removed "Following Naming Convention for Hostnames" (224)
- Removed "Use 429 With Header For Rate Limits" (153)
- Set "Secure All Endpoints With Scopes" (105) to SHOULD
- Set "Response As JSON Object" (110) to SHOULD
- Set "Use Problem JSON" (176) to SHOULD
- Set "Prefer Compatible Extensions" (107) to MAY
- Set "Pluralize Resource Names" (134) to SHOULD
- Add "CamelCaseInPropName" rule (hint: it's configured via CaseChecker in reference.conf)

## Todo
- Link to SBB ruleset (https://schweizerischebundesbahnen.github.io/api-principles/restful/)
- Clearify tracing headers:
  - (X-ProcessId, X-CorrelationId) vs (X-B3-ParentSpanId, X-B3-SpanId, X-B3-TraceId)
- Clearify headers in general (X-headers no longer en vogue)
  - B2P: X-Contract-Id, X-Conversation-Id
  - GPV: Request-Id, Correlation-Id, Process-Id
  - EDG: X-B3-*, X-SBB-distributionChannel