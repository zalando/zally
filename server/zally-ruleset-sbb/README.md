# How the SBB ruleset is created

The approach talen here is to start from Zalando's ruleset and then tailor it to SBB's need.

## Initial Steps
1. Copy from zally-ruleset-zalando
2. Refactor package names
3. Add directory 'zalando-ruleset-sbb' to 'settings.gradle.kts'
4. Rename 'ZalandoRuleSet' to 'SBBRuleSet'

## Coniguration of Rules
- Removed "Provide API Audience (219)"
- Removed "Following Naming Convention for Hostnames (224)"
