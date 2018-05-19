(ns io.hosaka.keychain.orchestrator
  (:require [io.hosaka.keychain.keys :as keys]
            [buddy.sign.jwt :as jwt]
            [manifold.deferred :as d]
            [cheshire.core :as json]
            [clj-crypto.core :as crypto]
            [clojure.string :refer [split join]]
            [com.stuartsierra.component :as component]))


(defrecord Orchestrator [db keys]
  component/Lifecycle

  (start [this]
    this)

  (stop [this]
    this))

(defn new-orchestrator []
  (component/using
   (map->Orchestrator {})
   [:keys :db]))

(defn key-to-pem [{:keys [key_data]}]
  (str "-----BEGIN PUBLIC KEY-----\n"
       (join "\n"
             (loop [data key_data lines []]
               (if (>= 48 (count data))
                 (conj lines data)
                 (recur
                  (subs data 48)
                  (conj lines (subs data 0 48))
                  ))))
       "\n-----END PUBLIC KEY-----"))

(defn get-public-key [{:keys [keys]} kid]
  (d/chain
   (keys/get-key keys kid)
   key-to-pem))

(defn bytes->string [b]
  (if b
    (String. b)
    nil))

(defn get-kid [token]
  (-> token
      (split #"\.")
      first
      crypto/decode-base64
      bytes->string
      (json/parse-string true)
      :kid))

(defn validate-token [{:keys [keys]} token]
  (if-let [kid  (get-kid token)]
    (d/chain
     (keys/get-key keys kid)
     #(if (nil? %1) (throw (Exception. (str "Unknown key: " kid))) %1)
     #(jwt/unsign token (:key %1) (select-keys %1 [:alg]))
     #(if (nil? %1) (throw (Exception. (str "Invalid key: " %1))) %1)
     #(if (= kid (:iss %1)) %1 (throw (Exception. (str "KID(" kid ") did not match ISS(" (:iss %1) ")")))))
    (d/error-deferred (Exception. "Invalid token"))
    )
  )
