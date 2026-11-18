# OpenRewrite recipes for the Boot 2.7 -> 3.5 migration

Companion to the `spring-boot4-migration` skill, which starts at Boot 3.5. These cover the part before it:
Boot 2.7 -> 3.5. Nothing here modifies `build.gradle.kts`: the plugin is injected with an init script, so there
is nothing to clean up afterwards.

**Status**

| Recipe | Dry run | Result |
|--------|---------|--------|
| `Step3_UpgradeTo35` | done, applied in stage 3 | 6 files; one value wrong (`access.default: read-only`, must be `unrestricted`), fixed by hand |
| `Step1_Junit5` | blocked, not run | unknown. `NullPointerExceptionTest` uses `SpringClassRule`/`SpringMethodRule`/`@Rule` in Kotlin and the Java-oriented recipe may skip it. |
| `Step2_Tooling` | blocked, not run | unknown. The Kotlin version is a text replace of `val kotlinVersion = "1.8.0"` in the two build files. |
| `Step3b_RemoveLegacyLibraries` | not run | untested |

### Why `Step3_UpgradeTo35` is a hand-picked subset

The first version used the official composite `org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_5`.
Its dry run on Zally (17 files) was mostly harmful:

- `ApiReview.kt`: wrote Java syntax `PersistentOffsetDateTime.class` into Kotlin (does not compile).
- `NoSecurityConfiguration.kt`: `override fun filterChain(...)` on a `WebSecurityConfigurerAdapter` that has no such
  method, `antMatchers` left in place. `OAuthConfiguration.kt`: half-converted to lambdas, still using
  `.requestMatchers().antMatchers().and()`, `.access("#oauth2...")` and the removed adapter. Both need a manual rewrite.
- `zally-rule-api`: replaced `swagger-models` with `swagger-models-jakarta` twice (2.2.7 and 2.2.55) and dropped
  `io.swagger:swagger-models:1.6.9`, although `io.swagger.models.Swagger` is still imported 5 times.
- `runtimeOnly("javax.xml.bind:jaxb-api:2.3.1")` added to 8 build files, including `buildSrc`, and a spurious
  `jakarta.annotation-api` in `zally-core`.
- `hibernate-core` pinned to an explicit `6.5.3.Final`, which would hold back Boot's managed version.
- Flyway was not touched (`flyway-core:9.11.0` is pinned) and `junit-vintage-engine` was removed while
  `NullPointerExceptionTest` still imports `org.junit.Rule`.

The useful parts are kept as explicit steps: Boot plugin version, `javax.persistence`/`javax.servlet` ->
`jakarta.*`, and the property migrations (`spring.profiles` -> `spring.config.activate.on-profile`,
`management.endpoints.enabled-by-default` -> `access.default`). `init.gradle` excludes the library modules and
`buildSrc`, which have no Spring dependency. The Boot version is a text replace instead of
`UpgradePluginVersion` because that recipe also rewrites `hibernate-core` to the old managed version.

Expected saving: roughly the Jakarta imports, the Boot version and two property keys. The security, Hibernate,
Flyway and test rewrites are manual in any case (list below).

Dead legacy keys in `application-dev.yml` / `application-test.yml` (`endpoints.*`, `security.basic.enabled`)
are not handled: delete them by hand.


## Usage

JDK 17 is required (Kotlin 1.8 does not know JDK 21). From `server/`:

```bash
# 1. preview, writes build/reports/rewrite/rewrite.patch and changes no source file
./gradlew --init-script openrewrite/init.gradle rewriteDryRun \
  -Drewrite.activeRecipe=org.zalando.zally.migration.Boot2.Step1_Junit5

# 2. apply
./gradlew --init-script openrewrite/init.gradle rewriteRun \
  -Drewrite.activeRecipe=org.zalando.zally.migration.Boot2.Step1_Junit5
```

Then review `git diff`, run `./gradlew build`, commit, and continue with the next recipe.

## Recipes (run in this order, one commit each)

| # | Recipe | What it does | Still manual afterwards |
|---|--------|--------------|-------------------------|
| 1 | `Step1_Junit5` | JUnit 4 -> 5 (SpringRunner, rules). Runs on Boot 2.7. | Verify `NullPointerExceptionTest` and `RestApiTestConfiguration`; drop `junit-vintage-engine`. |
| 2 | `Step2_Tooling` | Gradle wrapper -> 8.14.x, Kotlin plugins -> 2.2.x. | Dokka 2.x (`dokkaJavadoc` task is gone), ktlint/Spotless, kapt, pinned versions in the root `build.gradle.kts`. |
| 3 | `Step3_UpgradeTo35` | Boot plugin 3.5.16, javax -> jakarta (persistence, servlet), property renames. | See the "Manual work" list below. |
| 3b | `Step3b_RemoveLegacyLibraries` | Removes `stups-spring-oauth2-server`, `twintip-spring-web`, `problem-spring-web`, `usertype.core`. Opt-in: compilation fails on purpose. | Write the replacements. |

Pinned tool versions (`7.39.0` plugin, `rewrite-spring 6.31.0`) match the `spring-boot4-migration` skill.
Newer releases exist (plugin 7.41.0, rewrite-spring 6.37.1); bump them together with the skill.

## Manual work OpenRewrite will not do for Zally

- `OAuthConfiguration` / `NoSecurityConfiguration`: rewrite to `SecurityFilterChain` with the built-in resource
  server and a tokeninfo `OpaqueTokenIntrospector`. The legacy `#oauth2.hasScope('uid')` expression has no recipe.
- `ApiReview.kt`: `@Type(PersistentOffsetDateTime)` -> native `OffsetDateTime` mapping with UTC storage. The
  `created` column is `TIMESTAMP WITHOUT TIME ZONE` and `ddl-auto` is `validate`, so check the mapping.
- `ApiReviewRepository` HQL: `isSuccessfulProcessed = 'True'`, and the unqualified column names `day` and
  `user_agent` are rejected or behave differently under Hibernate 6.
- `WebMvcConfiguration`: `favorPathExtension(true)` (`.yaml` suffix negotiation) and trailing-slash URLs
  (`/health/`, `/api-violations/`). Spring 6 stops matching trailing slashes by default.
- Replace `twintip` (2 classes, `javax.servlet`) with a small controller serving `/api`.
- `application*.yml`: `spring.profiles: local`, `endpoints.*`, `security.basic.enabled`, `non_contextual_creation`.
- Pinned versions in `build.gradle.kts` (Jackson 2.14.1, snakeyaml 1.33, mockito, logback, junit 5.9.1, flyway
  9.11.0): explicit versions beat Boot's dependency management, so these silently hold the stack back.
