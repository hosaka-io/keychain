-- db/sql/keys.sql

-- :name get-key-sql :? :1
SELECT kid, authoritative, created_on, kty, crv, x, y, alg
FROM keychain.keys
WHERE kid = CAST(:kid AS UUID)

-- :name get-authoritative-keys-sql :? :*
SELECT kid, authoritative, kty, crv, x, y, alg
  FROM keychain.keys
 WHERE authoritative

-- :name add-key-sql :! :n
INSERT INTO keychain.keys(kid, authoritative, kty, use, crv, x, y, alg)
VALUES (CAST(:kid AS UUID), :authoritative, :kty, :use, :crv, :x, :y, :alg)
