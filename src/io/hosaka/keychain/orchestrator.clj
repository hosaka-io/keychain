(ns io.hosaka.keychain.orchestrator
  (:require [io.hosaka.keychain.keys :as keys]
            [io.hosaka.keychain.db.keys :as db-keys]
            [io.hosaka.keychain.keys :refer [generate-key-pair]]
            [buddy.sign.jwt :as jwt]
            [buddy.sign.jws :refer [decode-header]]
            [manifold.deferred :as d]
            [cheshire.core :as json]
            [clojure.tools.logging :as log]
            [clojure.string :refer [split join lower-case]]
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

(comment 
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
         "\n-----END PUBLIC KEY-----")))

(defn sanitize-key [{:keys [kid] :as key}]
  (assoc
    (select-keys key [:kty :use :crv :x :y :alg :authoritative])
    :kid (str kid)))

(defn get-public-key [{:keys [keys]} kid]
  (d/chain
   (keys/get-key keys kid)
   sanitize-key

   ))

(defn get-authoritative-keys [{:keys [keys]}]
  (d/let-flow [keys (keys/get-authoritative-keys keys)]
    (hash-map :keys (map sanitize-key keys))))

(defn get-kid [jwt]
  (->
   jwt
   decode-header
   :kid))

(defn unsign [& args]
  (try
    (apply jwt/unsign args)
    (catch Exception e
      (do
        (log/info e "Invalid token")
        nil))))

(defn validate-token [{:keys [keys]} token]
  (if-let [kid  (get-kid token)]
    (d/let-flow [{:keys [alg key authoritative]} (keys/get-key keys kid)]
      (if (nil? key)
        (throw (Exception. (str "Unknown key: " kid)))
        (if-let [claims (unsign token key {:alg (-> alg lower-case keyword)})]
          (if (= kid (:iss claims))
            (if (or (= (:iss claims) (:sub claims))
                    authoritative)
              claims
              (throw (Exception. "Token not signed by authoritative key")))
            (throw (Exception. (str "KID(" kid ") did not match ISS(" (:iss claims) ")"))))
          (throw (Exception. "Invalid token")))))
    (do
      (log/info "Malformed token")
      (d/error-deferred (Exception. "Invalid token")))))

(defn add-key [{:keys [db]} jwk user]
  (try
    (db-keys/add-key db jwk user)
    (catch AssertionError e (d/error-deferred e))))

(defn create-key [orchestrator id user]
  (let [{:keys [public-key private-key]} (generate-key-pair)]
    (d/chain
     (add-key orchestrator (assoc public-key :kid id) user)
     (fn [_] private-key))))


