#!/bin/bash

JAVA_PATH="/app/jdk/jdk-11.0.17/bin/java"
SENDER_NAME="LGHV01"
JAR_PATH="../../common/lib/kr.co.seoultel.lghv-1.1.0.jar"
CONF_PATH="../conf/application.yml"
LOG_CONF_PATH="../conf/logback-spring.xml"

nohup $JAVA_PATH -D$SENDER_NAME -Dfile.encoding=UTF-8 -server -Xms512m -Xmx512m -Xss256k -jar $JAR_PATH --spring.config.location=file:$CONF_PATH --logging.config=$LOG_CONF_PATH > /dev/null 2>&1 &



#!/bin/bash

JAVA_PATH="/app/jdk/jdk-11.0.17/bin/java"
SENDER_NAME="D_M_LG01"
JAR_PATH="../../../kr.co.seoultel.mms.direct-1.1.0.jar"
CONF_PATH="../conf/application.yml"
LOG_CONF_PATH="../conf/logback-spring.xml"

nohup $JAVA_PATH -D$SENDER_NAME -Dfile.encoding=UTF-8 -server -Xms512m -Xmx512m -Xss256k -jar $JAR_PATH --spring.config.location=file:$CONF_PATH --logging.config=$LOG_CONF_PATH > /dev/null 2>&1 &
