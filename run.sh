#!/bin/sh

U_ID="$(id -u)"
U_HOME="$(pwd)"
U_ASSEMBLY="${U_HOME}/target/multibot210-assembly-1.0.jar"
SVD_ASSEMBLY="/var/ServeD/svd.user/target/user-assembly-0.6.10-b35.jar"

if [ ! -f "${ASSEMBLY}" ]; then
  sbt assembly
fi

# -Djava.security.manager \

java \
-Dmultibot.production=true \
-Duser.name="${U_ID}" \
-Duser.home="${U_HOME}" \
-noverify \
-XX:+CMSClassUnloadingEnabled \
-Dfile.encoding=UTF-8 \
-XX:MaxPermSize=512m \
-Xmx1g \
-server \
-cp "${U_ASSEMBLY}:${SVD_ASSEMBLY}" \
org.multibot.Multibottest