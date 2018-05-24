-- db/sql/keys.sql

-- :name get-key-sql :? :1
SELECT kid, authoritative, created_on, kty, crv, x, y, alg
FROM keychain.keys
WHERE kid = CAST(:kid AS UUID)

-- :name get-authoritative-keys-sql :? :*
SELECT kid, authoritative, kty, crv, x, y, alg
  FROM keychain.keys
 WHERE authoritative
