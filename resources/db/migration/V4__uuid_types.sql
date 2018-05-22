ALTER TABLE keychain.keys
ALTER COLUMN kid TYPE uuid USING kid::uuid;
