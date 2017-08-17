CREATE TABLE validation (
  id             BIGSERIAL PRIMARY KEY,
  repository_url TEXT,
  api_definition TEXT,
  violations     TEXT,
  created_on     TIMESTAMP WITHOUT TIME ZONE NOT NULL
);
