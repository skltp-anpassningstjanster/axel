FROM alpine as builder
ENV MYSQLVERSION=8.0.30 \
    LOG4J_VERSION=2.22.0

ADD https://repo1.maven.org/maven2/mysql/mysql-connector-java/${MYSQLVERSION}/mysql-connector-java-${MYSQLVERSION}.jar \
    /opt/catalina/lib/

ADD https://repo1.maven.org/maven2/org/apache/logging/log4j/log4j-api/${LOG4J_VERSION}/log4j-api-${LOG4J_VERSION}.jar \
    https://repo1.maven.org/maven2/org/apache/logging/log4j/log4j-core/${LOG4J_VERSION}/log4j-core-${LOG4J_VERSION}.jar \
    https://repo1.maven.org/maven2/org/apache/logging/log4j/log4j-appserver/${LOG4J_VERSION}/log4j-appserver-${LOG4J_VERSION}.jar \
    https://repo1.maven.org/maven2/co/elastic/logging/log4j2-ecs-layout/1.5.0/log4j2-ecs-layout-1.5.0.jar \
    https://repo1.maven.org/maven2/co/elastic/logging/ecs-logging-core/1.5.0/ecs-logging-core-1.5.0.jar \
    https://repo1.maven.org/maven2/co/elastic/logging/jul-ecs-formatter/1.5.0/jul-ecs-formatter-1.5.0.jar \
    /opt/catalina/log4j2/lib/

COPY platforms/war/shs-broker-war/target/axel-shs-broker*.war /tmp/shs-broker.war
COPY platforms/war/riv-shs-war/target/axel-riv-shs*.war /tmp/riv-shs.war

RUN mkdir -p /opt/catalina/webapps/
RUN unzip /tmp/shs-broker.war -d /opt/catalina/webapps/shs-broker
RUN unzip /tmp/riv-shs.war -d /opt/catalina/webapps/riv-shs

ADD docker_context/logging.properties /opt/catalina/conf/
ADD docker_context/setenv.sh /opt/catalina/bin/
ADD docker_context/log4j2.xml /opt/catalina/log4j2/conf/log4j2-tomcat.xml
ADD docker_context/log4j2.xml docker_context/log4j.xml /opt/catalina/conf/


FROM tomcat:9-jre8-temurin AS axel
ENV APP_NAME=axel \
    AXEL_HOME=$CATALINA_HOME

COPY --from=builder /opt/catalina ${CATALINA_HOME}/

