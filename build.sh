#!/usr/bin/env sh

java -version
mvn -DforkCount=1 -DreuseForks=false -Dmaven.test.skip=true clean install -X