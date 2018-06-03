(ns io.hosaka.keychain.handler
  (:require [com.stuartsierra.component :as component]
            [io.hosaka.keychain.orchestrator :as orchestrator]
            [clojure.tools.logging :as log]
            [io.hosaka.common.db.health :as health]
            [cheshire.core :as json]
            [manifold.deferred :as d]
            [yada.yada :as yada]))

(defn get-public-key [orchestrator ctx]
  (let [kid (-> ctx :parameters :path :kid)]
    (orchestrator/get-public-key orchestrator kid)))

(defn validate-token [orchestrator {:keys [body response]}]
  (->
   (orchestrator/validate-token orchestrator body)
   (d/chain #(assoc response :body (json/generate-string %) :status 200 :headers {"content-type" "application/json"}))
   (d/catch #(let [msg (.getMessage %)]
               (log/warn (str "Invalid token: " msg))
               (assoc response :body (json/generate-string {:err true :msg msg}) :status 400 :headers {"content-type" "application/json"})))))

(defn get-authoritative-keys [orchestrator ctx]
  (orchestrator/get-authoritative-keys orchestrator))

(defn no-authorization-token [response]
  (assoc response :body {:error "No authorization token"} :status 401))

(defn add-key [orchestrator {:keys [body response request]}]
  (if-let [header (-> request :headers (get "authorization"))]
    (if-let [token (second (re-matches #"[Bb]earer: (.*)" header))]
      (->
       (d/let-flow [{:keys [iss sub roles]} (orchestrator/validate-token orchestrator token)]
         (if (and (not= iss sub)
                  ((set roles) "DEITY"))
             (->
              (orchestrator/add-key orchestrator body sub)
              (d/chain (fn [c] {:msg "Key added"}))
              (d/catch java.sql.SQLException
                  (fn [e]
                    (log/info "Error adding key" e)
                    (assoc response :body {:error "Invalid request"} :status 400)))
              (d/catch java.lang.AssertionError
                  (fn [e]
                    (log/info "Error adding key" e)
                    (assoc response :body {:error "Invalid JWK"} :status 400))))
           (assoc response :body {:error "Invalid authorization token"} :status 403)))
       (d/catch (fn [e]
                  (log/warn "Invalid token " e)
                  (no-authorization-token response))))
      (no-authorization-token response))
    (no-authorization-token response)))

(defn build-routes [orchestrator health]
  ["/" [
        ["keys/"
         (yada/resource {:methods
                         {:get
                          {:produces "application/json"
                           :response (partial get-authoritative-keys orchestrator)}
                          :put
                          {:produces "application/json"
                           :consumes "application/json"
                           :response (partial add-key orchestrator)}
                          :post
                          {:consumes "text/plain"
                           :response (partial validate-token orchestrator)}}})]
        ["keys"
         (yada/resource {:methods
                         {:get
                          {:produces "application/json"
                           :response (partial get-authoritative-keys orchestrator)}
                          :put
                          {:produces "application/json"
                           :consumes "application/json"
                           :response (partial add-key orchestrator)}
                          :post
                          {:consumes "text/plain"
                           :response (partial validate-token orchestrator)}}})]
        [["keys/" :kid]
         (yada/resource {:parameters {:path {:kid String}}
                         :methods
                         {:get
                          {:response (partial get-public-key orchestrator)
                           :produces "application/json"}}})]
        ["health"
         (yada/resource {:methods
                         {:get
                          {:response (partial health/get-health health)
                           :produces "application/json"}}})]
        ]])


(defrecord Handler [orchestrator routes health]
  component/Lifecycle

  (start [this]
    (assoc this :routes (build-routes orchestrator health)))

  (stop [this]
    (assoc this :routes nil)))


(defn new-handler []
  (component/using
   (map->Handler {})
   [:orchestrator :health])
)

