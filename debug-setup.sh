#!/bin/bash

 

mvn -U clean package
cd target
java -Dlogback.configurationFile="conf/debug-logback.xml" -Dconfig.file="conf/debug-application.conf" -server  -Xms1g -Xmx2g -classpath "./proteus-techempower-1.0.0.jar:lib/*" io.sinistral.ExampleApplication 
