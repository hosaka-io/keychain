(ns io.hosaka.keychain.keys
  (:require [clj-crypto.core :as crypto]
            [buddy.core.keys :as jwk]
            [manifold.deferred :as d]
            [io.hosaka.keychain.db.keys :as db-keys]
            [com.stuartsierra.component :as component]))

(defrecord Keys [db keys]
  component/Lifecycle

  (start [this]
    (assoc this :keys (atom {})))

  (stop [this]
    (hash-map)))

(defn new-keys []
  (component/using
   (map->Keys {})
   [:db]))

(defn decode-key [k]
  (if-let [key (jwk/jwk->public-key k)]
    (assoc k :key key)
    nil))

(defn get-key [keys kid]
  (if (-> keys :keys deref (contains? kid))
    (-> keys
        :keys
        deref
        (get kid)
        d/success-deferred)
    (d/chain
     (db-keys/get-key (:db keys) kid)
     decode-key
     (fn [k]
       (do
         (if k (swap! (-> keys :keys) #(assoc % kid k)))
         k)))))

(defn get-authoritative-keys [{:keys [db]}]
  (db-keys/get-authoritative-keys db))

(defn generate-key-pair []
  (let [k (crypto/generate-key-pair :key-size 256 :algorithm "ECDSA")
        kpm (crypto/get-key-pair-map k)]
    {:private-key (crypto/encode-base64-as-str (-> kpm :private-key :bytes))
     :public-key  (crypto/encode-base64-as-str (-> kpm :public-key  :bytes))}))

