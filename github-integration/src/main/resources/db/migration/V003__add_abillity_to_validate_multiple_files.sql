ALTER TABLE validation RENAME TO pull_request_validation;

ALTER TABLE pull_request_validation DROP COLUMN api_definition, DROP COLUMN violations;

ALTER TABLE pull_request_validation RENAME COLUMN id TO pull_request_validation_id;

CREATE TABLE api_validation (
  api_validation_id           BIGSERIAL PRIMARY KEY,
  pull_request_validation_id  BIGSERIAL,
  file_name                   TEXT,
  api_definition              TEXT,
  violations                  JSONB
);

ALTER TABLE api_validation ADD CONSTRAINT pull_request_validation_fk FOREIGN KEY (pull_request_validation_id) REFERENCES pull_request_validation