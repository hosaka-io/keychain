(ns io.hosaka.keychain.db.keys
  (:require [manifold.deferred :as d]
            [io.hosaka.common.db :refer [get-connection def-db-fns]]))

(def-db-fns "db/sql/keys.sql")

(defn get-key [db kid]
  (d/future
    (get-key-sql (get-connection db) {:kid kid})))

(defn get-authoritative-keys [db]
  (d/future
    (get-authoritative-keys-sql (get-connection db))))

