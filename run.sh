#!/bin/sh

U_ID="$(id -u)"
U_HOME="$(pwd)"
U_ASSEMBLY="${U_HOME}/target/multibot210-assembly-1.0.jar"

if [ ! -f "${U_ASSEMBLY}" ]; then
  sbt assembly
fi

unset JAVA_OPTS
# unset SBT_OPTS
java \
-Djava.security.manager \
-Dmultibot.production=true \
-Duser.name="${U_ID}" \
-Duser.home="${U_HOME}" \
-XX:+CMSClassUnloadingEnabled \
-Dfile.encoding=UTF-8 \
-XX:MaxPermSize=512m \
-Xmx1g \
-server \
-jar "${U_ASSEMBLY}"
