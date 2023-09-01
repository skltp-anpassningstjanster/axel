FROM tomcat:9-jdk8 as builder

RUN curl -s# https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.30/mysql-connector-java-8.0.30.jar -o lib/mysql-connector-java.jar

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
    AXEL_HOME=$CATALINA_HOME

COPY --from=builder $CATALINA_HOME/lib/mysql-connector-java.jar lib/mysql-connector-java.jar
COPY --from=builder $CATALINA_HOME/webapps/riv-shs/ $CATALINA_HOME/webapps/riv-shs/
COPY --from=builder $CATALINA_HOME/webapps/shs-broker/  $CATALINA_HOME/webapps/shs-broker/592738
