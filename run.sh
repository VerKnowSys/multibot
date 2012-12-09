#!/bin/sh

U_ID="$(id -u)"
U_HOME="$(pwd)"
U_ASSEMBLY="target/multibot-assembly-1.0.jar"

if [ ! -f "${ASSEMBLY}" ]; then
  sbt assembly
fi

java \
-Djava.security.manager \
-Dmultibot.production=true \
-Duser.name="${U_ID}" \
-Duser.home="${U_HOME}" \
-XX:+CMSClassUnloadingEnabled \
-jar ./target/multibot-assembly-1.0.jar
