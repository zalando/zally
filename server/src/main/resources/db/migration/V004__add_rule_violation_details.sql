ALTER TABLE rule_violation ADD description TEXT;
ALTER TABLE rule_violation ADD rule_title TEXT;
ALTER TABLE rule_violation ADD rule_url TEXT;
ALTER TABLE rule_violation ADD location_pointer TEXT;
ALTER TABLE rule_violation ADD location_line_start INT;
ALTER TABLE rule_violation ADD location_line_end INT;
