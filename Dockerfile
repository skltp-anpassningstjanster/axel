FROM tomcat:9-jdk8

ENV APP_NAME=axel \
    VERSION=2.1.3-SNAPSHOT \
    AXEL.HOME=$CATALINA_HOME
ENV WARFILE_SHS=platforms/war/riv-shs-war/target/axel-riv-shs-${VERSION}.war \
    WARFILE_BROKER=platforms/war/shs-broker-war/target/axel-shs-broker-${VERSION}.war

RUN curl https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.30/mysql-connector-java-8.0.30.jar -o lib/mysql-connector-java.jar

COPY docker/catalina_home/ $CATALINA_HOME
ADD $WARFILE_SHS $CATALINA_HOME/webapps/
ADD $WARFILE_BROKER $CATALINA_HOME/webapps/