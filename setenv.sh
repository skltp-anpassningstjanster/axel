
CATALINA_OPTS+=" -Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager"
CATALINA_OPTS+=" -Dlog4j.configurationFile=file:${CATALINA_HOME}/conf/log4j2.xml"
CATALINA_OPTS+=" -Dlog4j.debug=false"
export CATALINA_OPTS
export LOGGING_CONFIG="${CATALINA_HOME}/conf/log4j2.xml"
export CLASSPATH=$(echo ${CLASSPATH} ${CATALINA_HOME}/lib/log4j-{core,api,appserver,jul,layout-template-json}-${LOG4JVERSION}.jar|tr ' ' :)
rm $CATALINA_HOME/conf/logging.properties