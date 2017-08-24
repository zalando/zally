insert into pull_request_validation (
  pull_request_validation_id,
  pull_request_info,
  created_on)

values (
  100,
  '{
  "pull_request": {
    "url": "https://api.github.com/repos/myUserName/zally/pulls/1",
    "html_url": "https://github.com/myUserName/zally/pull/1",
    "title": "TEST",
    "head": {
      "ref": "experiment",
      "sha": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
      "user": {
        "login": "myUserName",
        "url": "https://api.github.com/users/myUserName",
        "html_url": "https://github.com/myUserName",
        "avatar_url": "https://avatars3.githubusercontent.com/u/189443?v=4"
      }
    }
  },
  "repository": {
    "name": "zally",
    "url": "https://api.github.com/repos/myUserName/zally",
    "html_url": "https://github.com/myUserName/zally"
  },
  "sender": {
    "login": "myUserName",
    "url": "https://api.github.com/users/myUserName",
    "html_url": "https://github.com/myUserName",
    "avatar_url": "https://avatars3.githubusercontent.com/u/189443?v=4"
  }
}',
  NOW()
);

INSERT INTO api_validation (
  api_validation_id,
  pull_request_validation_id,
  api_definition,
  violations
) VALUES (
    200,
    100,
    'swagger: ''2.0''

info:
 title: Zally
 description: Zalando''s API Linter
 version: "1.1.0"
 contact:
   name: Team API Management
   email: team-api-management@zalando.de

host: "zally.overarching.zalan.do"

schemes:
  - https

basePath: /

securityDefinitions:
  oauth2:
    type: oauth2
    tokenUrl: https://somewhere.on.the.internet
    flow: password
    scopes:
      uid: submit API specification for validation
',
    '{"message":null,"violations":[{"title":"Some Violation","description":"Some Description","violation_type":"SHOULD","rule_link":"http://example.com/some_violation","paths":["/abcde/"]}],"violations_count":null}'
);