alter table validation alter column pull_request_info type JSONB using pull_request_info::jsonb;
alter table validation alter column violations type JSONB using violations::jsonb;