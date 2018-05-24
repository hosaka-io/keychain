ALTER TABLE keychain.keys DROP COLUMN key_data;

ALTER TABLE keychain.keys DROP COLUMN algorithm;

ALTER TABLE keychain.keys
ALTER COLUMN x SET NOT NULL;

ALTER TABLE keychain.keys
ALTER COLUMN y SET NOT NULL;
