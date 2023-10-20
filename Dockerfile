FROM tomcat:9-jdk8 as builder

COPY platforms/war/shs-broker-war/target/axel-shs-broker*.war webapps/shs-broker.war
RUN mkdir webapps/shs-broker \
    && cd webapps/shs-broker \
    && jar xf ../shs-broker.war

COPY platforms/war/riv-shs-war/target/axel-riv-shs*.war webapps/riv-shs.war
RUN mkdir webapps/riv-shs \
    && cd webapps/riv-shs \
    && jar xf ../riv-shs.war


FROM tomcat:9-jdk8
ENV APP_NAME=axel \
    AXEL_HOME=$CATALINA_HOME \
    LOG4JVERSION=2.20.0 \
    MYSQLVERSION=8.0.30

ADD log4j2.xml ${CATALINA_HOME}/conf/log4j2.xml
ADD https://repo1.maven.org/maven2/org/apache/logging/log4j/log4j-appserver/${LOG4JVERSION}/log4j-appserver-${LOG4JVERSION}.jar \
    https://repo1.maven.org/maven2/org/apache/logging/log4j/log4j-api/${LOG4JVERSION}/log4j-api-${LOG4JVERSION}.jar \
    https://repo1.maven.org/maven2/org/apache/logging/log4j/log4j-core/${LOG4JVERSION}/log4j-core-${LOG4JVERSION}.jar \
    https://repo1.maven.org/maven2/org/apache/logging/log4j/log4j-jul/${LOG4JVERSION}/log4j-jul-${LOG4JVERSION}.jar \
    https://repo1.maven.org/maven2/org/apache/logging/log4j/log4j-layout-template-json/${LOG4JVERSION}/log4j-layout-template-json-${LOG4JVERSION}.jar \
    https://repo1.maven.org/maven2/mysql/mysql-connector-java/${MYSQLVERSION}/mysql-connector-java-${MYSQLVERSION}.jar \
    ${CATALINA_HOME}/lib/


COPY --from=builder ${CATALINA_HOME}/webapps/riv-shs/ ${CATALINA_HOME}/webapps/riv-shs/
COPY --from=builder ${CATALINA_HOME}/webapps/shs-broker/  ${CATALINA_HOME}/webapps/shs-broker/

