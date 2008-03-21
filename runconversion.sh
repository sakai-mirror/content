#!/bin/sh
#
# you could use the runconversion in db/db-util/conversion like this
# runconversion.sh -j $HOME/.m2/repository/org/sakaiproject/sakai-assignment-api/2.5.0/sakai-assignment-api-2.5.0.jar \
#        -j $HOME/.m2/repository/org/sakaiproject/sakai-assignment-api/2.5.0/sakai-assignment-api-2.5.0.jar  \
#          $@
#
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/commons-logging/commons-logging/1.0.4/commons-logging-1.0.4.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/commons-collections/commons-collections/3.2/commons-collections-3.2.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/commons-dbcp/commons-dbcp/1.2.2/commons-dbcp-1.2.2.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/commons-pool/commons-pool/1.3/commons-pool-1.3.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/sakaiproject/sakai-content-api/2.5.0/sakai-content-api-2.5.0.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/sakaiproject/sakai-content-impl/2.5.0/sakai-content-impl-2.5.0.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/sakaiproject/sakai-entity-api/2.5.0/sakai-entity-api-2.5.0.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/sakaiproject/sakai-entity-util/2.5.0/sakai-entity-util-2.5.0.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/sakaiproject/sakai-db-api/2.5.0/sakai-db-api-2.5.0.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/sakaiproject/sakai-db-storage/2.5.0/sakai-db-storage-2.5.0.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/sakaiproject/sakai-db-conversion/2.5.0/sakai-db-conversion-2.5.0.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/sakaiproject/sakai-component-api/2.5.0/sakai-component-api-2.5.0.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/sakaiproject/sakai-util-api/2.5.0/sakai-util-api-2.5.0.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/sakaiproject/sakai-util-util/2.5.0/sakai-util-util-2.5.0.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/sakaiproject/sakai-util/2.5.0/sakai-util-2.5.0.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/sakaiproject/sakai-util-log/2.5.0/sakai-util-log-2.5.0.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/log4j/log4j/1.2.9/log4j-1.2.9.jar"

##### JDBC DRIVER #####
##### SUPPLY PATH TO YOUR JDBC DRIVER #####
## MYSQL ##
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/mysql/mysql-connector-java/3.1.14/mysql-connector-java-3.1.14-bin.jar"
CLASSPATH="$CLASSPATH:$CATALINA_HOME/common/lib/mysql-connector-java-3.1.14-bin.jar"
CLASSPATH="$CLASSPATH:$CATALINA_HOME/shared/lib/mysql-connector-java-5.0.5-bin.jar"
## ORACLE ##
CLASSPATH="$CLASSPATH:$CATALINA_HOME/common/lib/ojdbc-14.jar"

java $JAVA_OPTS  \
      -classpath "$CLASSPATH" \
	org.sakaiproject.util.conversion.UpgradeSchema "$@" 
