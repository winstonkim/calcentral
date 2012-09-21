#!/bin/bash
# script to run calcentral

export MAVEN_OPTS="-Xmx512M -XX:MaxPermSize=128M"

function die {
	echo "$1" 1>&2
  exit 3
}

if [ -z "$1" ]; then
    echo "Usage: $0 source_root [logfile]"
    exit;
fi

SRC_LOC=$1
cd $SRC_LOC/calcentral
# disable history expansion so password with ! does not cause problems
set +H
set -e
mkdir -p logs
mkdir -p properties

INPUT_FILE="$SRC_LOC/.build.cf"
if [ -f $INPUT_FILE ]; then
  POSTGRES_PASSWORD=`awk -F"=" '/^POSTGRES_PASSWORD=/ {print $2}' $INPUT_FILE`
  APPLICATION_HOST=`awk -F"=" '/^APPLICATION_HOST=/ {print $2}' $INPUT_FILE`
  CAS_LOGOUT_HOST=`awk -F"=" '/^CAS_LOGOUT_HOST=/ {print $2}' $INPUT_FILE`
  CAS_LOGOUT_URL=`awk -F"=" '/^CAS_LOGOUT_URL=/ {print $2}' $INPUT_FILE`
  ORACLE_DB=`awk -F"=" '/^ORACLE_DB=/ {print $2}' $INPUT_FILE`
  ORACLE_USERNAME=`awk -F"=" '/^ORACLE_USERNAME=/ {print $2}' $INPUT_FILE`
  ORACLE_PASSWORD=`awk -F"=" '/^ORACLE_PASSWORD=/ {print $2}' $INPUT_FILE`
  ORACLE_URL=`awk -F"=" '/^ORACLE_URL=/ {print $2}' $INPUT_FILE`
  SAKAI2_HOST=`awk -F"=" '/^SAKAI2_HOST=/ {print $2}' $INPUT_FILE`
  SAKAI2_SECRET=`awk -F"=" '/^X_SAKAI_TOKEN_SHARED_SECRET=/ {print $2}' $INPUT_FILE`
  CANVAS_ROOT=`awk -F"=" '/^CANVAS_ROOT=/ {print $2}' $INPUT_FILE`
  CANVAS_SECRET=`awk -F"=" '/^CANVAS_SECRET=/ {print $2}' $INPUT_FILE`
  CANVAS_ACCOUNT_ID=`awk -F"=" '/^CANVAS_ACCOUNT_ID=/ {print $2}' $INPUT_FILE`
  CANVAS_OAUTH_CLIENT=`awk -F"=" '/^CANVAS_OAUTH_CLIENT=/ {print $2}' $INPUT_FILE`
  CANVAS_OAUTH_CLIENTSECRET=`awk -F"=" '/^CANVAS_OAUTH_CLIENTSECRET=/ {print $2}' $INPUT_FILE`
  CANVAS_OAUTH_ACCESSTOKENURI=`awk -F"=" '/^CANVAS_OAUTH_ACCESSTOKENURI=/ {print $2}' $INPUT_FILE`
  CANVAS_OAUTH_USERAUTHURI=`awk -F"=" '/^CANVAS_OAUTH_USERAUTHURI=/ {print $2}' $INPUT_FILE`
  GOOGLE_OAUTH_CLIENT=`awk -F"=" '/^GOOGLE_OAUTH_CLIENT=/ {print $2}' $INPUT_FILE`
  GOOGLE_OAUTH_CLIENTSECRET=`awk -F"=" '/^GOOGLE_OAUTH_CLIENTSECRET=/ {print $2}' $INPUT_FILE`
  GOOGLE_OAUTH_ACCESSTOKENURI=`awk -F"=" '/^GOOGLE_OAUTH_ACCESSTOKENURI=/ {print $2}' $INPUT_FILE`
  GOOGLE_OAUTH_USERAUTHURI=`awk -F"=" '/^GOOGLE_OAUTH_USERAUTHURI=/ {print $2}' $INPUT_FILE`
  GOOGLE_OAUTH_REDIRECTURI=`awk -F"=" '/^GOOGLE_OAUTH_REDIRECTURI=/ {print $2}' $INPUT_FILE`

else
  POSTGRES_PASSWORD='secret'
  APPLICATION_HOST='http://localhost:8080'
  CAS_LOGOUT_HOST='https://auth-test.berkeley.edu/cas/logout'
  CAS_LOGOUT_URL='http%3A%2F%2Flocalhost%3A8080%2F'
  SAKAI2_HOST='localhost:8080'
  SAKAI2_SECRET=POSTGRES_PASSWORD
fi

LOG=$2
if [ -z "$2" ]; then
    LOG=$SRC_LOC/calcentral/logs/run.log
fi
LOGIT="tee -a $LOG"

echo "=========================================" | $LOGIT
echo "`date`: CalCentral run started" | $LOGIT

echo | $LOGIT\

echo "`date`: Fetching new sources for CalCentral..." | $LOGIT
git pull >>$LOG 2>&1
echo "Last commit:" | $LOGIT
git log -1 | $LOGIT

echo | $LOGIT
echo "------------------------------------------" | $LOGIT
echo "Updating local configuration files..." | $LOGIT

CONFIG_FILES=`pwd`/properties

# put the config params into customPropsDir properties files
# this overwrites existing files!
echo "runDataSource.password=$POSTGRES_PASSWORD" > $CONFIG_FILES/dataSource.properties
echo "itDataSource.password=$POSTGRES_PASSWORD" >> $CONFIG_FILES/dataSource.properties
if [ $ORACLE_DB ]; then
  echo "campusDataSource.targetName=oracleDataSource" >> $CONFIG_FILES/dataSource.properties
  echo "oracleDataSource.url=jdbc:oracle:thin:@$ORACLE_URL:$ORACLE_DB" >> $CONFIG_FILES/dataSource.properties
  echo "oracleDataSource.username=$ORACLE_USERNAME" >> $CONFIG_FILES/dataSource.properties
  echo "oracleDataSource.password=$ORACLE_PASSWORD" >> $CONFIG_FILES/dataSource.properties
fi
echo "casAuthenticationFilter.serverName=$APPLICATION_HOST" > $CONFIG_FILES/calcentral.properties
echo "casValidationFilter.serverName=$APPLICATION_HOST" >> $CONFIG_FILES/calcentral.properties
echo "logoutSuccessHandler.defaultTargetUrl=$CAS_LOGOUT_HOST?url=$CAS_LOGOUT_URL" >> $CONFIG_FILES/calcentral.properties
echo "sakai2Proxy.sharedSecret=$SAKAI2_SECRET" >> $CONFIG_FILES/calcentral.properties
echo "sakai2Proxy.sakai2Host=$SAKAI2_HOST" >> $CONFIG_FILES/calcentral.properties
if [ $CANVAS_ROOT ]; then
  echo "canvasProxy.canvasRoot=$CANVAS_ROOT" >> $CONFIG_FILES/calcentral.properties
  echo "canvasProxy.adminAccessToken=$CANVAS_SECRET" >> $CONFIG_FILES/calcentral.properties
  echo "canvasProxy.accountId=$CANVAS_ACCOUNT_ID" >> $CONFIG_FILES/calcentral.properties
fi
if [ $CANVAS_OAUTH_CLIENT ]; then
  echo "canvasSecurity.clientId=$CANVAS_OAUTH_CLIENT" >> $CONFIG_FILES/calcentral.properties
  echo "canvasSecurity.clientSecret=$CANVAS_OAUTH_CLIENTSECRET" >> $CONFIG_FILES/calcentral.properties
  echo "canvasSecurity.accessTokenUri=$CANVAS_OAUTH_ACCESSTOKENURI" >> $CONFIG_FILES/calcentral.properties
  echo "canvasSecurity.userAuthorizationUri=$CANVAS_OAUTH_USERAUTHURI" >> $CONFIG_FILES/calcentral.properties
fi
if [ $GOOGLE_OAUTH_CLIENT ]; then
  echo "gappsSecurity.clientId=$GOOGLE_OAUTH_CLIENT" >> $CONFIG_FILES/calcentral.properties
  echo "gappsSecurity.clientSecret=$GOOGLE_OAUTH_CLIENTSECRET" >> $CONFIG_FILES/calcentral.properties
  echo "gappsSecurity.accessTokenUri=$GOOGLE_OAUTH_ACCESSTOKENURI" >> $CONFIG_FILES/calcentral.properties
  echo "gappsSecurity.userAuthorizationUri=$GOOGLE_OAUTH_USERAUTHURI" >> $CONFIG_FILES/calcentral.properties
  echo "gappsSecurity.pre-established-redirect-uri=$GOOGLE_OAUTH_REDIRECTURI" >> $CONFIG_FILES/calcentral.properties
fi

echo | $LOGIT
echo "------------------------------------------" | $LOGIT
echo "`date`: Stopping CalCentral..." | $LOGIT
mvn -B -e jetty:stop | $LOGIT

echo | $LOGIT
echo "------------------------------------------" | $LOGIT
echo "`date`: Building code..." | $LOGIT
mvn -B -e clean install -Dmaven.test.skip=true | $LOGIT

# initialize the db
echo | $LOGIT
echo "------------------------------------------" | $LOGIT
echo "Migrating the database..." | $LOGIT
mvn -e flyway:migrate -Dmaven.test.skip=true -Dflyway.password=$POSTGRES_PASSWORD | $LOGIT

echo | $LOGIT
echo "------------------------------------------" | $LOGIT
echo "`date`: Starting CalCentral..." | $LOGIT

# actually run the server (in the background)
nohup mvn -e jetty:run -Dmaven.test.skip=true >> logs/jetty.`date +\%Y_\%m_\%d`.log 2>&1 &

# wait 20s for server to get started
sleep 20;

echo | $LOGIT
echo "------------------------------------------" | $LOGIT
echo "`date`: Checking that server is alive..." | $LOGIT
echo "GET $APPLICATION_HOST"
curl -i -stderr /dev/null $APPLICATION_HOST | head -2 | $LOGIT | grep "HTTP/1.1 200 OK" || die "ERROR: Index page failed to respond 200 OK"
echo "GET $APPLICATION_HOST/api/currentUser"
curl -i -stderr /dev/null $APPLICATION_HOST/api/currentUser | head -2 | $LOGIT | grep "HTTP/1.1" || die "ERROR: currentUser page failed to respond"

echo | $LOGIT
echo "`date`: Reinstall complete." | $LOGIT
