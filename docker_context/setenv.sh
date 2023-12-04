export CLASSPATH=$CATALINA_HOME/log4j2/lib/*:$CATALINA_HOME/log4j2/conf

CATALINA_OPTS+=" -Dlog4j.configurationFile=\"file://${CATALINA_HOME}/conf/log4j2.xml\""
CATALINA_OPTS+=" -Dlog4j.configuration=\"file://${CATALINA_HOME}/conf/log4j.xml\""

if [ -n "$AXEL_HOME" ]; then
  CATALINA_OPTS+=" -Daxel.home=\"$AXEL_HOME\""
fi
if [ -n "$TAK_DB_LOGIN" ]; then
  CATALINA_OPTS+=" -Dtak.db.login=\"$TAK_DB_LOGIN\""
fi
if [ -n "$TAK_DB_URL" ]; then
  CATALINA_OPTS+=" -Dtak.db.url=\"$TAK_DB_URL\""
fi
if [ -n "$TAK_DB_PASSWORD" ]; then
  CATALINA_OPTS+=" -Dtak.db.password=\"$TAK_DB_PASSWORD\""
fi
if [ -n "$RS_ENDPOINT" ]; then
  CATALINA_OPTS+=" -DrsEndpoint=\"$RS_ENDPOINT\""
fi
if [ -n "$PRODUCT_SERVICE_REST_ENDPOINT" ]; then
  CATALINA_OPTS+=" -DproductServiceRestEndpoint=\"$PRODUCT_SERVICE_REST_ENDPOINT\""
fi
if [ -n "$NODEID" ]; then
  CATALINA_OPTS+=" -DnodeId=\"$NODEID\""
fi
if [ -n "$ORGID" ]; then
  CATALINA_OPTS+=" -DorgId=\"$ORGID\""
fi
if [ -n "$SHS_LABEL_STATUS" ]; then
  CATALINA_OPTS+=" -Dshs.label.status=\"$SHS_LABEL_STATUS\""
fi
if [ -n "$SHSRSKEYSTOREPARAMETERS_PASSWORD" ]; then
  CATALINA_OPTS+=" -DshsRsKeyStoreParameters.password=\"$SHSRSKEYSTOREPARAMETERS_PASSWORD\""
fi
if [ -n "$SHSRSTRUSTSTOREPARAMETERS_RESOURCE" ]; then
  CATALINA_OPTS+=" -DshsRsTrustStoreParameters.resource=\"$SHSRSTRUSTSTOREPARAMETERS_RESOURCE\""
fi
if [ -n "$SHSRSTRUSTSTOREPARAMETERS_TYPE" ]; then
  CATALINA_OPTS+=" -DshsRsTrustStoreParameters.type=\"$SHSRSTRUSTSTOREPARAMETERS_TYPE\""
fi
if [ -n "$SHSRSTRUSTSTOREPARAMETERS_PASSWORD" ]; then
  CATALINA_OPTS+=" -DshsRsTrustStoreParameters.password=\"$SHSRSTRUSTSTOREPARAMETERS_PASSWORD\""
fi
if [ -n "$SHSRSKEYSTOREPARAMETERS_RESOURCE" ]; then
  CATALINA_OPTS+=" -DshsRsKeyStoreParameters.resource=\"$SHSRSKEYSTOREPARAMETERS_RESOURCE\""
fi
if [ -n "$SHSRSKEYSTOREPARAMETERS_TYPE" ]; then
  CATALINA_OPTS+=" -DshsRsKeyStoreParameters.type=\"$SHSRSKEYSTOREPARAMETERS_TYPE\""
fi

export CATALINA_OPTS

echo $CATALINA_OPTS
