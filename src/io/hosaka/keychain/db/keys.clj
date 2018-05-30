(ns io.hosaka.keychain.db.keys
  (:require [manifold.deferred :as d]
            [clojure.spec.alpha :as s]
            [io.hosaka.common.spec :as specs]
            [io.hosaka.common.db :refer [get-connection def-db-fns] :as db]))

(def-db-fns "db/sql/keys.sql")

(defn get-key [db kid]
  {:pre [(s/valid? ::db/db db)
         (s/valid? ::specs/uuid kid)]}
  (d/future
    (get-key-sql (get-connection db) {:kid kid})))

(defn get-authoritative-keys [db]
  {:pre [(s/valid? ::db/db db)]}
  (d/future
    (get-authoritative-keys-sql (get-connection db))))

