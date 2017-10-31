#!/bin/bash
# This is some secure program that uses security.

# use as following
# parameter => possible  values
# env => dev, prod , uat

ENV=$1
if [ "$ENV" == "" ]; then
    ENV=dev
fi

java -Xms128m -Xmx256m -verbose:gc  -Dspring.environment=$ENV -Dlogging.config=../config/log4j2.xml -Dspring.config.location=../config/application.properties,../config/env/$ENV/application.properties -jar ../lib/NaviGateService-0.0.1-SNAPSHOT.jar

