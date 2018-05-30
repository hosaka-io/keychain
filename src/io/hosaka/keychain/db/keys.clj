(ns io.hosaka.keychain.db.keys
  (:require [manifold.deferred :as d]
            [clojure.spec.alpha :as s]
            [io.hosaka.common.spec :as specs]
            [io.hosaka.common.db :refer [get-connection def-db-fns] :as db]))

(def-db-fns "db/sql/keys.sql")

(s/def ::kid ::specs/uuid)
(s/def ::kty ::specs/non-empty-string)
(s/def ::use ::specs/non-empty-string)
(s/def ::crv ::specs/non-empty-string)
(s/def ::x ::specs/non-empty-string)
(s/def ::y ::specs/non-empty-string)
(s/def ::alg ::specs/non-empty-string)
(s/def ::authoritative boolean?)
(s/def ::jwk (s/keys :req-un [::kid ::kty ::use ::crv ::x ::y ::alg] :opt-un [::authoritative]))

(defn get-key [db kid]
  {:pre [(s/valid? ::db/db db)
         (s/valid? ::specs/uuid kid)]}
  (d/future
    (get-key-sql (get-connection db) {:kid kid})))

(defn get-authoritative-keys [db]
  {:pre [(s/valid? ::db/db db)]}
  (d/future
    (get-authoritative-keys-sql (get-connection db))))

(defn add-key [db jwk user]
  {:pre [(s/valid? ::db/db db)
         (s/valid? ::specs/uuid user)
         (s/valid? ::jwk jwk)]}
  (d/future
    (add-key-sql (get-connection db) (merge
                                      {:authoritative false}
                                      jwk
                                      {:created_by user}))))
