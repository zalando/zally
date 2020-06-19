# How the SBB ruleset is created

The approach talen here is to start from Zalando's ruleset and then tailor it to SBB's need.

## Initial Steps
1. Copy from zally-ruleset-zalando
2. Refactor package names
3. Add directory 'zalando-ruleset-sbb' to 'settings.gradle.kts'
4. Rename 'ZalandoRuleSet' to 'SBBRuleSet'

## Coniguration of Rules
- Removed "Provide API Audience" (219)
- Removed "Following Naming Convention for Hostnames" (224)
- Removed "Use 429 With Header For Rate Limits" (153)
- Set "Secure All Endpoints With Scopes" (105) to SHOULD
- Set "Response As JSON Object" (110) to SHOULD
- Set "Use Problem JSON" (176) to SHOULD