(ns io.hosaka.common.db.health
  (:require [manifold.deferred :as d]
            [io.hosaka.keychain.db :refer [get-connection def-db-fns]]))

(def-db-fns "db/sql/health.sql")

(defn get-db-health [db]
  (d/future
    (get-db-health-sql (get-connection db))))

