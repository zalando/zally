CREATE TABLE validation (
  id                BIGSERIAL PRIMARY KEY,
  pull_request_info TEXT,
  api_definition    TEXT,
  violations        TEXT,
  created_on        TIMESTAMP WITHOUT TIME ZONE NOT NULL
);
