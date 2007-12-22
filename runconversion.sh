#!/bin/sh
#
# you could use the runconversion in db/db-util/conversion like this
# runconversion.sh -j $HOME/.maven/repository/sakaiproject/jars/sakai-content-api-${SAKAI_VERSION}.jar \
#        -j $HOME/.maven/repository/sakaiproject/jars/sakai-content-api-${SAKAI_VERSION}.jar  \
#          $@
#

SAKAI_VERSION=2-4-x
CLASSPATH="$CLASSPATH:$HOME/.maven/repository/commons-logging/jars/commons-logging-1.0.4.jar"
CLASSPATH="$CLASSPATH:$HOME/.maven/repository/commons-dbcp/jars/commons-dbcp-1.2.1.jar"
CLASSPATH="$CLASSPATH:$HOME/.maven/repository/commons-pool/jars/commons-pool-1.3.jar"
CLASSPATH="$CLASSPATH:$HOME/.maven/repository/commons-collections/jars/commons-collections-3.1.jar"
CLASSPATH="$CLASSPATH:$HOME/.maven/repository/sakaiproject/jars/sakai-content-api-${SAKAI_VERSION}.jar"
CLASSPATH="$CLASSPATH:$HOME/.maven/repository/sakaiproject/jars/sakai-content-impl-${SAKAI_VERSION}.jar"
CLASSPATH="$CLASSPATH:$HOME/.maven/repository/sakaiproject/jars/sakai-entity-api-${SAKAI_VERSION}.jar"
CLASSPATH="$CLASSPATH:$HOME/.maven/repository/sakaiproject/jars/sakai-entity-util-${SAKAI_VERSION}.jar"
CLASSPATH="$CLASSPATH:$HOME/.maven/repository/sakaiproject/jars/sakai-db-api-${SAKAI_VERSION}.jar"
CLASSPATH="$CLASSPATH:$HOME/.maven/repository/sakaiproject/jars/sakai-db-storage-${SAKAI_VERSION}.jar"
CLASSPATH="$CLASSPATH:$HOME/.maven/repository/sakaiproject/jars/sakai-db-conversion-${SAKAI_VERSION}.jar"
CLASSPATH="$CLASSPATH:$HOME/.maven/repository/sakaiproject/jars/sakai-component-api-${SAKAI_VERSION}.jar"
CLASSPATH="$CLASSPATH:$HOME/.maven/repository/sakaiproject/jars/sakai-util-api-${SAKAI_VERSION}.jar"
CLASSPATH="$CLASSPATH:$HOME/.maven/repository/sakaiproject/jars/sakai-util-util-${SAKAI_VERSION}.jar"
CLASSPATH="$CLASSPATH:$HOME/.maven/repository/sakaiproject/jars/sakai-util-${SAKAI_VERSION}.jar"
CLASSPATH="$CLASSPATH:$HOME/.maven/repository/sakaiproject/jars/sakai-util-log-${SAKAI_VERSION}.jar"
CLASSPATH="$CLASSPATH:$HOME/.maven/repository/log4j/jars/log4j-1.2.9.jar"

##### JDBC DRIVER #####
##### SUPPLY PATH TO YOUR JDBC DRIVER #####
## For example: ##
## MYSQL ##
CLASSPATH="$CLASSPATH:$HOME/.maven/repository/mysql/jars/mysql-connector-java-3.1.14-bin.jar"
CLASSPATH="$CLASSPATH:$CATALINA_HOME/common/lib/mysql-connector-java-3.1.14-bin.jar"
CLASSPATH="$CLASSPATH:$CATALINA_HOME/shared/lib/mysql-connector-java-5.0.5-bin.jar"
## ORACLE ##
CLASSPATH="$CLASSPATH:/Users/jreng/ojdbc14.jar"

java $JAVA_OPTS  \
      -classpath "$CLASSPATH" \
	org.sakaiproject.util.conversion.UpgradeSchema "$@" 
