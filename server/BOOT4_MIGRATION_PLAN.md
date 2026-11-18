# Zally server: Spring Boot 2.7 -> 4.0 migration plan

Status: baseline captured, OpenRewrite 2 -> 3 recipes dry-run (step 3 only), all six decisions settled.
Nothing else started.
Scope: `server/` (Gradle, Kotlin). `cli/` and `web-ui/` are untouched, but they are the API consumers, so they
are part of the gates.

Route: **2.7.7 -> 3.5.x -> 4.0.x**, one PR per stage. The `spring-boot4-migration` skill (in `~/.claude/skills`)
starts at Boot 3.5, so it covers stage 4 only.

## 0. Baseline (done)

Commit `6846db56` on `main`, JDK Temurin 17.0.20, Gradle 8.0-rc-1. The machine only has JDK 21/25 and Kotlin 1.8
rejects those, so JDK 17 is required for every build in stages 0-3.

| Module | Tests | Failed | Skipped | Line % | Branch % |
|---|---|---|---|---|---|
| zally-server | 995 | 0 | 1 | 88.2 | 76.8 |
| zally-core | 98 | 0 | 0 | 63.6 | 51.0 |
| zally-rule-api | 2 | 0 | 0 | 25.0 | n/a |
| zally-ruleset-zalando | 224 | 0 | 0 | 98.6 | 73.9 |
| zally-ruleset-zally | 81 | 0 | 1 | 95.3 | 75.3 |
| **Total** | **1400** | **0** | **2** | | |

`./gradlew spotlessCheck dokkaJavadoc` also passes. CI runs `./gradlew build`, which includes both.

Known uncovered code: `OAuthConfiguration` 0/26 lines (production profile is never tested), `ApplicationKt`,
`ViolationStatistic`.

### Gate for every stage

A stage is done only when all of these hold:

1. `./gradlew build` is green, including `spotlessCheck` and the Dokka/javadoc jar.
2. Test count is not below 1400 and skipped is not above 2, unless the change is explained in the PR.
3. Line and branch coverage per module does not drop more than 1 point against the table above.
4. Golden-master responses (stage 1) match, apart from differences listed and approved in the PR.
5. CLI integration tests pass: CI already runs `cli/zally/test.sh integration` against `bootRun` on port 8000.
6. Docker smoke test: build the image, run it against Postgres (compose uses Postgres 13), check `/health` on
   port 7979, one `POST /api-violations`, and that the jar is still named `zally-server.jar`
   (`version = ""` in `zally-server/build.gradle.kts` and the Dockerfile depend on it).

## 1. Safety net, on Boot 2.7 (one PR, merged first)

Today the production profile and Postgres are untested, so the later stages would have no safety net.

- **Postgres test (Testcontainers):** run the Flyway migrations V001-V007 and round-trip an `ApiReview`, including
  `created` (timestamp and timezone), `externalId` (UUID), the statistics query, and `findByExternalId`.
- **Upgrade-compatibility test:** write rows with the 2.7 build, read them with the 3.5 build. This is the check
  that Hibernate 6 can read what Jadira wrote (`TIMESTAMP WITHOUT TIME ZONE`, UTC).
- **Security tests on the `production` profile** (stub tokeninfo server): valid token with `uid` scope -> 200;
  no token -> 401 problem+json; wrong scope -> 403; `OPTIONS` and `/health` public; anything else denied.
  Fix `RestCorsWithOAuthTest`, which runs on the `test` profile (`permitAll`) and never exercises OAuth.
- **Golden master:** capture status, headers and JSON for `/api-violations` (POST, GET, with and without trailing
  slash), `/supported-rules`, `/review-statistics`, `/api`, `/.well-known/schema-discovery` (twintip),
  `/health`, `/metrics`, and the error cases (400, 401, 403, 404, 405, malformed JSON, unmapped route). Pin the
  exact error JSON (`title`, `status`, `detail` and nothing else) as a test, since decision 4 keeps that shape.
  Include `.yaml` suffix and `Accept: application/x-yaml` behaviour (`favorPathExtension(true)` in
  `WebMvcConfiguration`): decision 3 removes the suffix, so the diff must show up as an approved change.
- **Actuator test** on the management port.
- **JUnit 4 -> 5:** `NullPointerExceptionTest` (`SpringClassRule`, `SpringMethodRule`, `@Rule`) and
  `RestApiTestConfiguration`; then drop `junit-vintage-engine`. Run OpenRewrite `Step1_Junit5` (dry run first,
  untested), expect to finish by hand.
- Cover `ApplicationKt` and `ViolationStatistic`, and the 7 uncovered branches in `ReviewStatisticsController`.

## 2. Tooling, still on Boot 2.7 (one PR)

- Gradle 8.0-rc-1 -> 8.14.x (Boot 4 plugin needs 8.14+ or 9.x).
- Kotlin 1.8.0 -> 2.2.x. The version is a local `val kotlinVersion` in two build files. OpenRewrite `Step2_Tooling`
  replaces the text (untested).
- Dokka 1.7 -> 2.x. `dokkaJavadoc` no longer exists, and the root build's `javadocJar` uses it.
- ktlint 0.47.1 -> a version that parses Kotlin 2.2. Reformat in a separate formatting-only commit and add it to
  `.git-blame-ignore-revs`.
- kapt: check `auto-service` and the custom `RuleProcessor` still run.
- Library modules (core, rule-api, rulesets, zally-test) are published: set Kotlin `languageVersion`/`apiVersion`
  so consumers on an older Kotlin compiler can still read the metadata, or state the requirement.
- **Pinned versions audit:** Jackson 2.14.1, snakeyaml 1.33, Mockito 4.11, logback 1.4.5, JUnit 5.9.1, Flyway 9.11,
  json-schema-core forced to 1.2.14. With the Spring dependency-management plugin, a version declared on the
  dependency beats the Boot-managed one, so these silently hold the stack back. Decide per pin: remove (let Boot
  manage) or keep with a reason. `zally-test` exposes JUnit 5.9.1 and logback as `api`.
- **Result (done, `boot4/2-tooling`):** Gradle 8.14.6, Kotlin 2.2.21, Dokka 2.2.0 (`dokkaGeneratePublicationJavadoc`),
  spotless 7.2.1 with ktlint 1.7.1. kapt, `auto-service` and `RuleProcessor` still generate the service files.
  The library modules compile with `languageVersion`/`apiVersion` 1.8; the server uses 2.2. The reformat is its own
  commit, listed in `.git-blame-ignore-revs`.
- **Pinned versions audit.** On Boot 2.7 the dependency-management plugin lets a version declared on the dependency
  win, so most pins cannot be dropped here without a downgrade. Decision per pin:

  | Pin | Boot 2.7 manages | Decision |
  |---|---|---|
  | Jackson 2.14.1 (root + buildSrc) | 2.13.4 | keep; drop in stage 3 (let Boot 3.5 manage it) |
  | snakeyaml 1.33 | 1.30 | keep; drop in stage 3 (let Boot 3.5 manage it) |
  | Mockito 4.11.0 | 4.5.1 | keep; stage 3 moves to Mockito 5 |
  | Flyway 9.11.0 | 8.5.13 | keep; stage 3 moves to Flyway 10+ with `flyway-database-postgresql` |
  | json-schema-core forced 1.2.14 | not managed | keep; swagger-parser pulls an older one (commit 65e62d6d "Fix dependencies") |
  | logback 1.4.5 in `zally-test` | 1.2.11 | **inert in the server** (Boot's 1.2.11 wins; 1.4 needs slf4j 2). Only library consumers see it. Revisit in stage 3 |
  | JUnit 5.9.1 in `zally-test` and `zally-rule-api` | 5.8.2 | **inert in the server** (resolves to 5.8.2). Align with the managed version in stage 3, JUnit 6 in stage 4 |
- Gate: all of section "Gate for every stage" unchanged against the baseline.

## 3. Boot 2.7 -> 3.5.x (one PR)

**Automated (OpenRewrite `Step3_UpgradeTo35`, dry run done, 5 files):** Boot plugin 3.5.16, `javax.persistence` and
`javax.servlet` -> `jakarta.*`, `spring.profiles` -> `spring.config.activate.on-profile`,
`management.endpoints.enabled-by-default` -> `access.default`.

The official composite `UpgradeSpringBoot_3_5` is **not** used. On Zally it wrote invalid Kotlin (`Foo.class`),
half-converted the security configs, pinned `hibernate-core`, duplicated `swagger-models-jakarta` and added
`javax.xml.bind:jaxb-api` to 8 build files. Details in `server/openrewrite/README.md`.

**Manual, in this order:**

1. **Hibernate 6:**
   - Remove Jadira; `ApiReview.created`: native `OffsetDateTime` with UTC storage. The column is
     `TIMESTAMP WITHOUT TIME ZONE` and `ddl-auto` is `validate`, and Hibernate 6 defaults to
     `timestamp with time zone`, so startup fails without the right setting.
   - `ApiReviewRepository.getReviewStatistics`: `isSuccessfulProcessed = 'True'` (boolean vs string), unqualified
     column names `day` and `user_agent` in JPQL, aliases inside the constructor expression. Expect rejection.
   - Remove the `hibernate-entitymanager` excludes and the `non_contextual_creation` workaround.
2. **Security 6:** rewrite `OAuthConfiguration` and `NoSecurityConfiguration` as `SecurityFilterChain` beans with
   the built-in opaque-token resource server and a custom introspector for the tokeninfo endpoint (scope ->
   authorities, `#oauth2.hasScope('uid')` -> `hasAuthority("SCOPE_uid")`). Use public libraries only: the Zalando
   resource-server library from the skill's matrix lives on `maven.zalando.net` behind the VPN, and Zally is a public
   repo with public CI and Maven Central publishing.
3. **MVC 6 behaviour changes (decision 3):** trailing-slash matching is off by default and `.yaml`/`.json` suffix
   negotiation goes away. We break both deliberately: remove `favorPathExtension(true)` from `WebMvcConfiguration`,
   simplify the security matchers (no `/?` regex tails), keep `Accept: application/x-yaml` negotiation, and release-note it.
   The golden master from stage 1 shows the exact before/after.
4. **Library replacements:** `twintip-spring-web` (2 classes, `javax.servlet`, last release 2021) -> small
   controller for `/api` and schema discovery; `problem-spring-web` (last release 2023) -> own
   `ResponseEntityExceptionHandler` advice emitting `{title,status,detail}` as `application/problem+json`
   (decision 4). OpenRewrite `Step3b_RemoveLegacyLibraries` removes the dependencies and breaks compilation on purpose.
   The Zalando `problem` jar and `ProblemModule` go away too, which unblocks Jackson 3 in stage 4.
5. **Flyway 10+:** drop the explicit 9.11.0 pin, add `flyway-database-postgresql`; confirm the Postgres version
   in production (compose uses 13) is supported by the Flyway version Boot manages.
6. **Config cleanup:** delete dead legacy keys in `application-dev.yml` and `application-test.yml`
   (`endpoints.*`, `security.basic.enabled`).
7. **Tests:** `PathMatchersTest` (javax mocks, `AntPathRequestMatcher`), Mockito 5 (inline mock maker), the
   Jadler-based tests.
8. Compile with `-Xlint:deprecation` and clear the warnings now (skill Phase 3 rule: everything deprecated in 3.5
   is gone in 4.0). Known: `WebSecurityConfigurerAdapter`, `favorPathExtension`.

Gate: general gate, plus the upgrade-compatibility test from stage 1.

**Result (done, `boot4/3-boot35`):** Boot 3.5.16 (Hibernate 6.6, Spring Security 6.5, Flyway 11, Jackson 2.21,
snakeyaml 2.4), Undertow stays until stage 4. Gate: 1463 tests, 0 failed,
1 skipped (the existing one in `zally-ruleset-zally`; the server's old `@Disabled` matcher test no longer exists after
the `PathMatchersTest` rewrite, which is why skipped went from 2 to 1); CLI integration tests and the Docker smoke test (Postgres 13,
`dev` profile) pass.

Differences from Boot 2.7 that the golden master and the production tests show (all intended or unavoidable):

| Area | Boot 2.7 | Boot 3.5 | Why |
|---|---|---|---|
| Trailing slash (`/supported-rules/`, `/api-violations/`, `/api/`, ...) | 200 (405 for `GET /api-violations/`) | 404; in the production profile 403 | decision 3 |
| `.yaml` / `.json` path suffix | 404 or 400 already | 404 or 400, same status | nothing removed; only the `content-disposition` header is gone |
| No token (production) | 500 problem+json | **401** problem+json, `WWW-Authenticate: Bearer` | an authentication failure is a 401 |
| Token without `uid` scope | 401 `application/json` | 401 `application/problem+json` | own handler, same shape as other errors |
| Path denied for a valid token | 500 "Access is denied" | **403** "Access Denied" | an authorization failure is a 403 |
| Rejected by tokeninfo | 401, `Bearer realm="oauth2-resource"` | 401, standard RFC 6750 `WWW-Authenticate` | built-in resource server |
| Tokeninfo endpoint fails (5xx, timeout, 404, 429) | 401 (the stups library turned any failure into an invalid token) | **503** problem+json, no `WWW-Authenticate` | the token was never checked, so it is not the caller's fault; only 400/401/403 from tokeninfo mean "bad token" |
| Body `detail` of 401/403 | exception message | fixed text | do not echo the tokeninfo backend's wording |
| `/health/`, `/metrics/` on the management port | allowed | `/health/` 401 | trailing slash (decision 3); `/health` is what the health check uses |
| Framework error `detail` wording | "... nested exception is ..." | shorter Spring 6 wording | unavoidable, see Error body |
| `ApiReview.created` | JVM-zone wall clock (correct only in a UTC JVM) | always UTC | `hibernate.jdbc.time_zone=UTC`; existing rows are valid |

Notes for the next stage and for reviewers:
- `Origin` CORS preflight and the `Allow` header order differ trivially.
- `stups-spring-oauth2-server`, `problem-spring-web`, `twintip-spring-web` and Jadira are gone; replaced by
  `TokenInfoOpaqueTokenIntrospector`, `ExceptionHandling`, `SchemaController` and a native Hibernate mapping.
- `swagger-parser` (io.swagger 1.0.64, via zally-core) calls `SafeConstructor()`, which snakeyaml 2 removed: the server
  constrains it to 1.0.76. This was found by the existing tests, not by compilation.
- zally-core's `ReverseAstBuilder` appended an empty pointer segment for extension methods; Jackson 2.19 no longer
  drops the trailing slash, so it now keeps the pointer instead.
- Kotlin warnings left for later: annotation use-site targets on constructor properties (Kotlin 2.2 will apply
  them to the field too), and `kapt` running in K1 mode in the library modules because they keep language version 1.8.
- The upgrade-compatibility test from stage 1 is covered by the Postgres test reading rows written the old way
  (`TIMESTAMP WITHOUT TIME ZONE`, UTC) under four JVM time zones.

## 4. Boot 3.5 -> 4.0.x (one PR, follows the `spring-boot4-migration` skill)

Skill phases map as: 2 baseline (done at 3.5, re-run), 3 deprecations (done in stage 3), 4 OpenRewrite,
5 Zalando libraries (only `problem-spring-web`, already removed), 6 validate, 7 finalise, 8 post-migration
deprecations, 9 report.

- **Dry run first:** run `rewriteDryRun` and read the patch before `rewriteRun`. Stage 3 showed the composites
  misbehave on Kotlin and on this build layout. The skill says never revert OpenRewrite changes; if the patch is
  wrong, narrow the recipe list instead (as in stage 3) and say so in the PR.
- **Git:** the skill reserves git for the user. Do not commit during these phases.
- **Servlet container:** Undertow is gone (Servlet 6.1). Remove `spring-boot-starter-undertow`, use Tomcat.
- **Starters:** `spring-boot-starter-web` -> `-webmvc`; add `spring-boot-starter-flyway`; test side:
  `-webmvc-test`, `@AutoConfigureMockMvc` is now explicit, `TestRestTemplate` needs
  `@AutoConfigureTestRestTemplate` and the `spring-boot-resttestclient` dependency. `spring-boot-starter-classic`
  can bridge temporarily.
- **Jackson (decision 2: Jackson 3):** Boot 4 defaults to Jackson 3 (`tools.jackson`). Migrate the two custom
  `ObjectMapper` beans (JSON and YAML, snake-case, `WRITE_DATES_AS_TIMESTAMPS` off, `FAIL_ON_UNKNOWN_PROPERTIES` off),
  the YAML converter in `WebMvcConfiguration` (needs the Jackson 3 YAML module) and `JsonRawValueDeserializer`. DTO
  annotations (`JsonProperty`, `JsonInclude`, ...) keep the `com.fasterxml.jackson.annotation` package. No bridge. Core and
  rulesets stay on Jackson 2 because swagger-parser needs it; the server hands them Strings only. The golden master
  guards the JSON and YAML output. Spring plans to drop Jackson 2 support in 7.2.
- **JUnit:** Spring 7 expects JUnit 6; align `zally-test` and any pinned JUnit 5 versions.
- **Spring Security 7:** the stage 3 `SecurityFilterChain` should carry over; re-check removed matcher classes
  (`AntPathRequestMatcher` is gone in favour of `PathPatternRequestMatcher`), which also affects `PathMatchersTest`.
- **Spring Framework 7:** path-extension content negotiation and trailing-slash options are removed; `RestTemplate`
  (used in `ApiDefinitionReader`) is deprecated, not removed, so leave it and note it.
- **Hibernate 7 / JPA 3.2:** re-run the Postgres and compatibility tests.
- Gate: general gate, plus a manual Docker run.

**Result (done as a patch, `boot4/4-boot40`):** Boot 4.0.9 (Spring 7.0.10, Security 7.0.8, Hibernate 7.2, Jackson 3.1,
Tomcat 11, JUnit 6), JDK 17. `OpenRewrite UpgradeSpringBoot_4_0` was dry-run first, read, then applied; it worked from Maven
Central without VPN and was kept, with these corrections by hand:
- The recipe's Gradle output was reverted for the root `build.gradle.kts` (it rewrote the library modules' Jackson 2 pins to
  Jackson 3 and added `jaxb-api`) and cleaned up in `zally-server/build.gradle.kts` (duplicate `jackson-databind`,
  explicit `:3.5.16`/`:4.0.9` versions, Undertow kept at 3.5.16). Undertow is dropped: Boot 4 runs Tomcat 11.
- The two `ObjectMapper` beans are `JsonMapper`/`YAMLMapper` built with the builder (Jackson 3 mappers are immutable);
  only `jackson-module-kotlin` is added, Jackson 3 has the `java.time`, `Jdk8` and parameter-name support built in.
  The YAML converter is Spring's `JacksonYamlHttpMessageConverter`, registered through `configureMessageConverters`
  (`extendMessageConverters` is deprecated). It must also support `application/yaml`.
- Core and the rulesets stay on Jackson 2 (swagger-parser). Tests that touch core's `JsonNode`/`JsonPointer` use the Jackson 2
  types; the recipe had renamed them and `textValue()` to the Jackson 3 `asString()`.
- `swagger-parser-v3` 2.1.12 also calls `SafeConstructor()` and logged `Error snake-parsing yaml content` for every YAML
  definition (the review still worked via fallback). Constrained to 2.1.22 in the server.
- Starters: `-webmvc`, `-flyway`, `-restclient`, `-security-oauth2-resource-server`; tests: `-webmvc-test`, `-data-jpa-test`,
  `spring-boot-resttestclient`, `testcontainers-*`.

Gate: 1468 tests, 0 failed, 1 skipped (same as stage 3); coverage equal. CLI integration tests pass against `bootRun`, Docker
smoke test passes on Postgres 13 (Hibernate 7.2, Tomcat, `created` still UTC, jar still `zally-server.jar`, 85 MB).

Behaviour differences from Boot 3.5 (golden master, 24 of 90 cases changed, most only a header):
| Area | Boot 3.5 | Boot 4.0 |
|---|---|---|
| `Vary: Origin, Access-Control-Request-...` on error responses | present | gone (21 cases) |
| `Content-Disposition: inline;filename=f.txt` on `.yaml`/`.json` suffix requests | gone | back (4 cases); still 404/400 |
| `POST /api-violations` with `Accept: text/html` (406) | no `Location` | `Location` header present |
| `/health` body | `{"status":"UP"}` | `liveness`/`readiness` groups added; switched off with `management.endpoint.health.probes.enabled=false` to keep the body |
| 401 `WWW-Authenticate` | `Bearer` | `Bearer resource_metadata="http://.../.well-known/oauth-protected-resource"` |
| Preflight for a method the resource does not allow | 403 | 200 with no `Access-Control-*` headers (browsers refuse it the same) |
| JSON parse error text for a wrongly typed list | "Cannot construct instance of `java.util.ArrayList`..." | "Cannot deserialize value of type `java.util.ArrayList<java.lang.String>`..." |

Not done / follow-ups: the `production` profile has still not been started from the packaged jar; the real tokeninfo endpoint
has never been called (stub only); Kotlin warnings about annotation use-site targets remain; JDK 17 is kept (decision 1).

## 5. Cleanup

- Remove temporary compat shims (classic starters, Jackson 2 bridge if used) and `spring-boot-properties-migrator`.
- Re-run coverage against the baseline table and record the final numbers.
- Update README/docs, `delivery.yaml` and the Dockerfile if the JDK changes.
- Out of scope but stale, decide separately: `oss.sonatype.org` publishing URLs, `jitpack.yml` (`openjdk8`),
  `codecov-action@v1`, Go 1.16 in CI.

## Decisions (all settled)

| # | Decision | Choice | Consequence in the plan |
|---|----------|--------|-------------------------|
| 1 | Target JDK | **Stay on 17** | No change to CI, `delivery.yaml`, Dockerfile base image, `jvmTarget`. A JDK bump is a separate follow-up PR. JaCoCo 0.8.8 stays. |
| 2 | Jackson | **Jackson 3 in the server** (stage 4) | Migrate the 2 `ObjectMapper` beans, YAML converter and `JsonRawValueDeserializer` (about 5 main files). Core and rulesets stay on Jackson 2 (swagger-parser); safe because the server only passes Strings to them. |
| 3 | Trailing slash / `.yaml` suffix | **Break deliberately, keep `Accept`-header YAML** | Drop `favorPathExtension`, simplify the security matchers to slash-less paths, release-note the change. CLI and web-ui never use either. The golden master records the old behaviour so the diff is explicit. |
| 4 | Error body | **Keep today's 3-field shape, own advice** | See "Error body" below. Full `ProblemDetail` (adds `type`, `instance`) can be a later, announced change. |
| 5 | Deployment config names | **Keep all names stable** | `TOKEN_INFO_URI`, `MANAGEMENT_PORT` (default 7979), `SPRING_PROFILES_ACTIVE`, `SPRING_DATASOURCE_*`, `zally.*`, `rules-config-path`, `spring.oauth2.resource.tokenInfoUri`; actuator base-path `/` with health and metrics. Existing deployments change only the image tag. |
| 6 | Merge cadence | **Stacked PRs, one per stage; release at Boot 3.5** | Stages 0-3 are stacked PRs (each targets the previous stage's branch) and merge into `main` together, once stage 3 passes the full gate, so that Boot 3.5 can be released. Stage 4 (Boot 4.0) is a separate stack started from the released 3.5. |

### Error body (decision 4)

Documented contract: `zally-api.yaml` declares every error as `application/problem+json` using the Zalando
`Problem` schema. Observed on the 2.7 server: only `title`, `status`, `detail` are sent (404 for an unknown review
has no `detail`; an unmapped route returns Boot's default `{timestamp,status,error,path}` as `application/json`).
The web-ui reads `error.detail`; the CLI does not parse error bodies.

Implementation: a `@ControllerAdvice` extending `ResponseEntityExceptionHandler` that writes only
`{title, status, detail}` as `application/problem+json`, no Zalando library, no `ProblemModule`. Keep the four
custom exception handlers (missing definition, inaccessible URL, insufficient/future time parameter) and the
`ApiReviewNotFoundException` -> 404 without `detail`. Unmapped routes: keep the current Boot error JSON unless the
golden master shows a reason to change it. Known unavoidable difference: Spring 6+ wording for framework errors
(the "nested exception is com.fasterxml..." text disappears). The golden master records every such diff.

### Merge cadence (decision 6)

Stacked branches, each PR targeting the previous one:

| PR | Branch | Base | Content |
|---|---|---|---|
| 0 | `boot4/0-plan` | `main` | this plan, OpenRewrite recipes, CI trigger for `boot4/**` |
| 1 | `boot4/1-safety-net` | `boot4/0-plan` | stage 1 |
| 2 | `boot4/2-tooling` | `boot4/1-safety-net` | stage 2 |
| 3 | `boot4/3-boot35` | `boot4/2-tooling` | stage 3 |

Every PR must pass the general gate on its own. Nothing merges until PR 3 is green; then merge bottom-up,
retargeting each PR to `main` as the one below it lands, and release Boot 3.5. Stage 4 starts from that release.
A fix to a lower stage means rebasing the stages above it. Drop `'boot4/**'` from the CI trigger when the stack has
merged.

## Risks

| Risk | Where | Mitigation |
|---|---|---|
| Auth rewrite untested today | stage 3 | stage 1 production-profile security tests |
| Hibernate 6 rejects the HQL or the timestamp mapping at startup | stage 3 | Postgres + upgrade-compatibility tests |
| Trailing-slash / `.yaml` clients break (accepted, decision 3) | stage 3 | golden master shows the diff; release note; CLI and web-ui verified unaffected |
| OpenRewrite output wrong on Kotlin | stages 1-4 | always dry run, read the patch, narrow the recipe list |
| Pinned versions hold the stack back or conflict with Boot-managed ones | stages 2-4 | pin audit in stage 2 |
| Library consumers hit Kotlin/Jackson/JUnit version changes | stage 2, 4 | language/api version, release notes |
| HSQLDB passes where Postgres fails | all | Testcontainers Postgres in CI |
