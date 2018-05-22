-- db/sql/keys.sql

-- :name get-key-sql :? :1
SELECT kid, key_data, authoritative, created_on, algorithm
  FROM keychain.keys
 WHERE kid = CAST(:kid AS UUID)
