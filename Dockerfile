FROM tomcat:9-jdk8

ENV APP_NAME=axel \
    VERSION=2.1.4-SNAPSHOT \
    AXEL.HOME=$CATALINA_HOME

RUN curl https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.30/mysql-connector-java-8.0.30.jar -o lib/mysql-connector-java.jar

COPY platforms/war/riv-shs-war/target/axel-riv-shs*.war $CATALINA_HOME/webapps/
COPY platforms/war/shs-broker-war/target/axel-shs-broker*.war $CATALINA_HOME/webapps/