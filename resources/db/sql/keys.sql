-- db/sql/keys.sql

-- :name get-key-sql :? :1
SELECT kid, key_data, can_add, created_on, algorithm
  FROM keychain.keys
 WHERE kid = :kid
