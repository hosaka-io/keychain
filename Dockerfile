FROM openjdk:8
COPY ./target/uberjar/keychain.jar /srv/keychain.jar
WORKDIR /srv

ENTRYPOINT /usr/bin/java -Dconfig==`ls /run/secrets/*keychain_secret.edn` -jar /srv/keychain.jar