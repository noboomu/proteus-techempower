#!/bin/bash

fw_depends postgresql mysql java maven

sed -i 's|localhost|'"${DBHOST}"'|g' conf/application.conf

mvn clean package
cd target
java -Dlogback.configurationFile="conf/logback.xml" -server  -Xms1g -Xmx2g -XX:+UseG1GC -XX:+AggressiveOpts -XX:-UseBiasedLocking -XX:+UseStringDeduplication -classpath "./proteus-techempower-1.0.0.jar:lib/*" io.sinistral.ExampleApplication &