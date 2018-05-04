(ns io.hosaka.keychain.keys
  (:require [clj-crypto.core :as crypto]
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

(def size->alg {256 :es256 512 :es512})
(defn get-alg [k]
  (->> k
       .getParameters
       .getCurve
       .getFieldSize
       (get size->alg)))

(defn decode-key [k]
  (if-let [key (crypto/decode-public-key
                (assoc (select-keys k [:algorithm])
                       :bytes (crypto/decode-base64 (:key_data k))))]
    (assoc k :key key :alg (get-alg key))
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


(defn generate-key-pair []
  (let [k (crypto/generate-key-pair :key-size 256 :algorithm "ECDSA")
        kpm (crypto/get-key-pair-map k)]
    {:private-key (crypto/encode-base64-as-str (-> kpm :private-key :bytes))
     :public-key  (crypto/encode-base64-as-str (-> kpm :public-key  :bytes))}))

