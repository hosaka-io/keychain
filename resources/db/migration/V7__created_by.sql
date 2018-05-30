ALTER TABLE keychain.keys
 ADD COLUMN IF NOT EXISTS created_by uuid;

UPDATE keychain.keys
SET created_by = CAST('00000000-0000-0000-0000-000000000000' AS UUID)
WHERE created_by IS NULL;

ALTER TABLE keychain.keys
ALTER COLUMN created_by SET NOT NULL
