#!/bin/sh
#
# Usage:
#   content-runconversion.sh -j JDBC_DRIVER_JAR -p SAKAI_PROPERTIES_FILE UPGRADESCHEMA_CONFIG
#
# Example:
#   content-runconversion.sh -j "$CATALINA_HOME/shared/lib/ojdbc14.jar" \
#      -p "$CATALINA_HOME/sakai/sakai.properties" \
#      upgradeschema-oracle.config

# The "runconversion.sh" script does not have to be run
# in place. It could be copied to the working directory.

# For Cygwin, ensure paths are in the proper format.
cygwin=false;
case "`uname`" in
  CYGWIN*) cygwin=true ;;
esac
if $cygwin; then
  [ -n "$CLASSPATH" ] && CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
  m2repository=`cygpath --path --unix "$HOMEDRIVE""$HOMEPATH"`/.m2/repository
else
  m2repository="$HOME"/.m2/repository
fi

echo HOME:$HOME

bash ../db/db-util/conversion/runconversion.sh \
	-j "$m2repository"/commons-collections/commons-collections/3.2/commons-collections-3.2.jar \
	-j "$m2repository"/commons-pool/commons-pool/1.3/commons-pool-1.3.jar \
	-j "$m2repository"/org/sakaiproject/sakai-db-api/2.5.6/sakai-db-api-2.5.6.jar \
	-j "$m2repository"/org/sakaiproject/sakai-component-api/2.5.6/sakai-component-api-2.5.6.jar \
	-j "$m2repository"/org/sakaiproject/sakai-util/2.5.6/sakai-util-2.5.6.jar \
	-j "$m2repository"/org/sakaiproject/sakai-util-api/2.5.6/sakai-util-api-2.5.6.jar \
	-j "$m2repository"/org/sakaiproject/sakai-util-impl/2.5.6/sakai-util-impl-2.5.6.jar \
	-j "$m2repository"/org/sakaiproject/sakai-entity-api/2.5.6/sakai-entity-api-2.5.6.jar \
	-j "$m2repository"/org/sakaiproject/sakai-entity-util/2.5.6/sakai-entity-util-2.5.6.jar \
	-j "$m2repository"/org/sakaiproject/sakai-content-api/2.5.6/sakai-content-api-2.5.6.jar \
	-j "$m2repository"/org/sakaiproject/sakai-content-impl/2.5.6/sakai-content-impl-2.5.6.jar \
        -j "$m2repository"/org/sakaiproject/sakai-db-conversion/2.5.6/sakai-db-conversion-2.5.6.jar \
        -j "$m2repository"/org/sakaiproject/sakai-util-log/2.5.6/sakai-util-log-2.5.6.jar \
        -j "$m2repository"/log4j/log4j/1.2.9/log4j-1.2.9.jar \
	$@
