ALTER TABLE keychain.keys ADD COLUMN IF NOT EXISTS kty text;
ALTER TABLE keychain.keys ADD COLUMN IF NOT EXISTS use text;
ALTER TABLE keychain.keys ADD COLUMN IF NOT EXISTS crv text;
ALTER TABLE keychain.keys ADD COLUMN IF NOT EXISTS x text;
ALTER TABLE keychain.keys ADD COLUMN IF NOT EXISTS y text;
ALTER TABLE keychain.keys ADD COLUMN IF NOT EXISTS alg text;

UPDATE keychain.keys
SET kty = 'EC', use = 'sig', crv = 'P-256', alg = 'ES256'
WHERE algorithm = 'ECDSA' AND alg IS NULL;

ALTER TABLE keychain.keys ALTER COLUMN kty SET NOT NULL;
ALTER TABLE keychain.keys ALTER COLUMN use SET NOT NULL;
ALTER TABLE keychain.keys ALTER COLUMN crv SET NOT NULL;
ALTER TABLE keychain.keys ALTER COLUMN alg SET NOT NULL;
