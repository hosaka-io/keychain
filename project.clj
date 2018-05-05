(defproject io.hosaka/keychain "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories ^:replace [["releases" "https://artifactory.i.hosaka.io/artifactory/libs-release"]
                           ["snapshots" "https://artifactory.i.hosaka.io/artifactory/libs-snapshot"]]
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/tools.nrepl "0.2.13"]
                 [org.clojure/spec.alpha "0.1.143"]

                 [org.apache.logging.log4j/log4j-core "2.11.0"]
                 [org.apache.logging.log4j/log4j-api "2.11.0"]
                 [org.apache.logging.log4j/log4j-slf4j-impl "2.11.0"]
                 [org.clojure/tools.logging "0.4.0"]


                 [yogthos/config "1.1.1"]
                 [cheshire "5.8.0"]
                 [com.stuartsierra/component "0.3.2"]

                 [buddy "2.0.0"]
                 [buddy/buddy-sign "3.0.0.x"]
                 [buddy/buddy-core "1.5.0.x"]
                 [clj-crypto "1.0.2"
                  :exclusions [org.bouncycastle/bcprov-jdk15on bouncycastle/bcprov-jdk16]]
                 [clj-time "0.14.2"]

                 [ring/ring-core "1.6.2"]
                 [ring/ring-defaults "0.3.1"]

                 [com.layerware/hugsql "0.4.8"]
                 [hikari-cp "2.3.0"]
                 [org.postgresql/postgresql "42.2.2"]
                 [org.flywaydb/flyway-core "5.0.7"]

                 [yada "1.2.11"]
                 [aleph "0.4.5-alpha6"]
                 [manifold "0.1.7-alpha6"]

                 [com.novemberain/langohr "5.0.0"]
                 ]
  :main ^:skip-aot io.hosaka.keychain
  :target-path "target/%s"
  :source-paths ["src"]
  :profiles {:uberjar {:aot :all}
             :dev {:resource-paths ["env/dev/resources" "resources"]
                   :env {:dev true}}})
