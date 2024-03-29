FROM alpine:3 AS builder

COPY platforms/war/shs-broker-war/target/axel-shs-broker*.war /tmp/shs-broker.war
COPY platforms/war/riv-shs-war/target/axel-riv-shs*.war /tmp/riv-shs.war

RUN mkdir -p /opt/catalina/webapps/
RUN unzip /tmp/shs-broker.war -d /opt/catalina/webapps/shs-broker
RUN unzip /tmp/riv-shs.war -d /opt/catalina/webapps/riv-shs

FROM docker.drift.inera.se/ntjp/tomcat:9-jre8-log4j-ecs AS axel
USER ${APP_USER}
ENV APP_NAME=axel \
    AXEL_HOME=$CATALINA_HOME

COPY --from=builder /opt/catalina ${CATALINA_HOME}/

