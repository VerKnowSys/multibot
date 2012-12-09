#!/bin/sh

U_ID="$(id -u)"
U_HOME="$(pwd)"
U_ASSEMBLY="${U_HOME}/target/multibot210-assembly-1.0.jar"

if [ ! -f "${ASSEMBLY}" ]; then
  sbt assembly
fi

java \
-Djava.security.manager \
-Dmultibot.production=true \
-Duser.name="${U_ID}" \
-Duser.home="${U_HOME}" \
-XX:+CMSClassUnloadingEnabled \
-jar "${U_ASSEMBLY}"
