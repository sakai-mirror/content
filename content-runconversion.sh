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
	-j "$m2repository"/org/sakaiproject/sakai-db-api/sakai-2.5.4/sakai-db-api-sakai-2.5.4.jar \
	-j "$m2repository"/org/sakaiproject/sakai-component-api/sakai-2.5.4/sakai-component-api-sakai-2.5.4.jar \
	-j "$m2repository"/org/sakaiproject/sakai-util/sakai-2.5.4/sakai-util-sakai-2.5.4.jar \
	-j "$m2repository"/org/sakaiproject/sakai-util-api/sakai-2.5.4/sakai-util-api-sakai-2.5.4.jar \
	-j "$m2repository"/org/sakaiproject/sakai-util-impl/sakai-2.5.4/sakai-util-impl-sakai-2.5.4.jar \
	-j "$m2repository"/org/sakaiproject/sakai-entity-api/sakai-2.5.4/sakai-entity-api-sakai-2.5.4.jar \
	-j "$m2repository"/org/sakaiproject/sakai-entity-util/sakai-2.5.4/sakai-entity-util-sakai-2.5.4.jar \
	-j "$m2repository"/org/sakaiproject/sakai-content-api/sakai-2.5.4/sakai-content-api-sakai-2.5.4.jar \
	-j "$m2repository"/org/sakaiproject/sakai-content-impl/sakai-2.5.4/sakai-content-impl-sakai-2.5.4.jar \
        -j "$m2repository"/org/sakaiproject/sakai-db-conversion/sakai-2.5.4/sakai-db-conversion-sakai-2.5.4.jar \
        -j "$m2repository"/org/sakaiproject/sakai-util-log/sakai-2.5.4/sakai-util-log-sakai-2.5.4.jar \
        -j "$m2repository"/log4j/log4j/1.2.9/log4j-1.2.9.jar \
	$@
