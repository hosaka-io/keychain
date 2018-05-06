FROM openjdk:8
COPY ./target/uberjar/keychain.jar /srv/keychain.jar
WORKDIR /srv

ENTRYPOINT /usr/bin/java -jar /srv/keychain.jar