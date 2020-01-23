ALTER TABLE api_review ADD external_id UUID;

CREATE INDEX api_review_external_id_idx ON api_review (external_id)
