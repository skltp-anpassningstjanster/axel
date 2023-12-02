export CLASSPATH=$CATALINA_HOME/log4j2/lib/*:$CATALINA_HOME/log4j2/conf


CATALINA_OPTS+=" -Dlog4j.configuration=file://${CATALINA_HOME}/conf/log4j.xml"
CATALINA_OPTS+=" -Dlog4j.configurationFile=file://${CATALINA_HOME}/conf/log4j2.xml"

