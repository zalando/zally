# Overview of Checks
Please note, that checks implement only some aspects of respective guideline rule and cannot verify the API specification.

## [Zalando RESTful API and Event Scheme Guidelines](http://zalando.github.io/restful-api-guidelines/) Checks

| Check Name | Description | Related Guidline Rule |
|------------|-------------|------------------------
| AvoidLinkHeadersRule                  | Avoid Link in Header Rule                                     | [166](http://zalando.github.io/restful-api-guidelines/#166) |
| AvoidSynonymsRule                     | Use common property names                                     | [174](http://zalando.github.io/restful-api-guidelines/#174) |
| AvoidTrailingSlashesRule              | Avoid trailing slashes                                        | [136](http://zalando.github.io/restful-api-guidelines/#136) |
| CommonFieldTypesRule                  | Use common field names                                        | [174](http://zalando.github.io/restful-api-guidelines/#174) |
| DefineOAuthScopesRule                 | Define and Assign Access Rights (Scopes)                      | [104](http://zalando.github.io/restful-api-guidelines/#104) |
| EverySecondPathLevelParameterRule     | Every Second Path Level To Be Parameter                       | [143](http://zalando.github.io/restful-api-guidelines/#143) |
| ExtensibleEnumRule                    | Prefer Compatible Extensions                                  | [107](http://zalando.github.io/restful-api-guidelines/#107) |
| FormatForNumbersRule                  | Define Format for Type Number and Integer                     | [171](http://zalando.github.io/restful-api-guidelines/#171) |
| HyphenateHttpHeadersRule              | Use Hyphenated HTTP Headers                                   | [131](http://zalando.github.io/restful-api-guidelines/#131) |
| InvalidApiSchemaRule                  | OpenAPI 2.0 schema                                            | [101](http://zalando.github.io/restful-api-guidelines/#101) |
| KebabCaseInPathSegmentsRule           | Lowercase words with hyphens                                  | [129](http://zalando.github.io/restful-api-guidelines/#129) |
| LimitNumberOfResourcesRule            | Limit number of Resources                                     | [146](http://zalando.github.io/restful-api-guidelines/#146) |
| LimitNumberOfSubresourcesRule         | Limit number of Sub-resources level                           | [147](http://zalando.github.io/restful-api-guidelines/#147) |
| MediaTypesRule                        | Prefer standard media type names                              | [172](http://zalando.github.io/restful-api-guidelines/#172) |
| NestedPathsMayBeRootPathsRule         | Consider Using (Non-) Nested URLs                             | [145](http://zalando.github.io/restful-api-guidelines/#145) |
| NotSpecifyStandardErrorCodesRule      | Not Specify Standard Error Codes                              | [151](http://zalando.github.io/restful-api-guidelines/#151) |
| NoVersionInUriRule                    | Do Not Use URI Versioning                                     | [115](http://zalando.github.io/restful-api-guidelines/#115) |
| PascalCaseHttpHeadersRule             | Prefer Hyphenated-Pascal-Case for HTTP header fields          | [132](http://zalando.github.io/restful-api-guidelines/#132) |
| PluralizeNamesForArraysRule           | Array names should be pluralized                              | [120](http://zalando.github.io/restful-api-guidelines/#120) |
| PluralizeResourceNamesRule            | Pluralize Resource Names                                      | [134](http://zalando.github.io/restful-api-guidelines/#134) |
| QueryParameterCollectionFormatRule    | Explicitly define the Collection Format of Query Parameters   | [154](http://zalando.github.io/restful-api-guidelines/#154) |
| SecureWithOAuth2Rule                  | Secure Endpoints with OAuth 2.0                               | [104](http://zalando.github.io/restful-api-guidelines/#104) |
| SnakeCaseForQueryParamsRule           | Use snake_case (never camelCase) for Query Parameters         | [130](http://zalando.github.io/restful-api-guidelines/#130) |
| SnakeCaseInPropNameRule               | snake_case property names                                     | [128](http://zalando.github.io/restful-api-guidelines/#128) |
| SuccessResponseAsJsonObjectRule       | Response As JSON Object                                       | [110](http://zalando.github.io/restful-api-guidelines/#110) |
| Use429HeaderForRateLimitRule          | Use 429 With Header For Rate Limits                           | [153](http://zalando.github.io/restful-api-guidelines/#153) |
| UsePasswordFlowWithOauth2Rule         | Set Flow to Password When Using OAuth 2.0                     | [104](http://zalando.github.io/restful-api-guidelines/#104) |
| UseProblemJsonRule                    | Use Problem JSON                                              | [176](http://zalando.github.io/restful-api-guidelines/#176) |
| UseSpecificHttpStatusCodes            | Use Specific HTTP Status Codes                                | [150](http://zalando.github.io/restful-api-guidelines/#150) |
| VersionInInfoSectionRule              | Provide version information                                   | [116](http://zalando.github.io/restful-api-guidelines/#116) |
| NoUnusedDefinitionsRule               | Do not leave unused definitions                               | -   |
| NoProtocolInHostRule                  | Host should not contain protocol                              | Open API Spec |
