ALTER TABLE keychain.keys ADD COLUMN IF NOT EXISTS algorithm text;

UPDATE keychain.keys SET algorithm = 'ECDSA' WHERE algorithm IS NULL;

ALTER TABLE keychain.keys ALTER COLUMN algorithm SET NOT NULL;
