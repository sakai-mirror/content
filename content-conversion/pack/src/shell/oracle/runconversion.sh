#!/bin/sh
#
#
CLASSPATH=
LIB=../lib

## Must provide sakai-version and path to java-home 
SAKAI_VERSION=2-4-x
export JAVA_HOME=/usr/local/java/jdk1.5.0_10

## Must provide one (and only one) jdbc connector 
CLASSPATH="$CLASSPATH:${LIB}/ojdbc14.jar"

CLASSPATH="$CLASSPATH:${LIB}/commons-codec-1.3.jar"
CLASSPATH="$CLASSPATH:${LIB}/commons-collections-3.1.jar"
CLASSPATH="$CLASSPATH:${LIB}/commons-dbcp-1.2.1.jar"
CLASSPATH="$CLASSPATH:${LIB}/commons-logging-1.0.4.jar"
CLASSPATH="$CLASSPATH:${LIB}/commons-pool-1.3.jar"
CLASSPATH="$CLASSPATH:${LIB}/log4j-1.2.9.jar"
CLASSPATH="$CLASSPATH:${LIB}/sakai-component-api-${SAKAI_VERSION}.jar"
CLASSPATH="$CLASSPATH:${LIB}/sakai-content-api-${SAKAI_VERSION}.jar"
CLASSPATH="$CLASSPATH:${LIB}/sakai-content-impl-${SAKAI_VERSION}.jar"
CLASSPATH="$CLASSPATH:${LIB}/sakai-content-util-${SAKAI_VERSION}.jar"
CLASSPATH="$CLASSPATH:${LIB}/sakai-db-api-${SAKAI_VERSION}.jar"
CLASSPATH="$CLASSPATH:${LIB}/sakai-db-impl-${SAKAI_VERSION}.jar"
CLASSPATH="$CLASSPATH:${LIB}/sakai-db-conversion-${SAKAI_VERSION}.jar"
CLASSPATH="$CLASSPATH:${LIB}/sakai-db-storage-${SAKAI_VERSION}.jar"
CLASSPATH="$CLASSPATH:${LIB}/sakai-entity-api-${SAKAI_VERSION}.jar"
CLASSPATH="$CLASSPATH:${LIB}/sakai-entity-impl-${SAKAI_VERSION}.jar"
CLASSPATH="$CLASSPATH:${LIB}/sakai-entity-util-${SAKAI_VERSION}.jar"
CLASSPATH="$CLASSPATH:${LIB}/sakai-util-${SAKAI_VERSION}.jar"
CLASSPATH="$CLASSPATH:${LIB}/sakai-util-api-${SAKAI_VERSION}.jar"
CLASSPATH="$CLASSPATH:${LIB}/sakai-util-log-${SAKAI_VERSION}.jar"

env $JAVA_HOME/bin/java $JAVA_OPTS  -classpath "$CLASSPATH" org.sakaiproject.util.conversion.UpgradeSchema "$@"
