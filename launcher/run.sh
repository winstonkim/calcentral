#!/bin/bash
# script to run calcentral

export MAVEN_OPTS="-Xmx512M -XX:MaxPermSize=128M"

if [ -z "$2" ]; then
    echo "Usage: $0 source_root logfile"
    exit;
fi

SRC_LOC=$1

INPUT_FILE="$SRC_LOC/.build.cf"
if [ -f $INPUT_FILE ]; then
  POSTGRES_PASSWORD=`awk -F"=" '/^POSTGRES_PASSWORD=/ {print $2}' $INPUT_FILE`
else
  POSTGRES_PASSWORD='secret'
fi

LOG=$2
if [ -z "$2" ]; then
    LOG=/dev/null
fi
LOGIT="tee -a $LOG"

echo "=========================================" | $LOGIT
echo "`date`: CalCentral run started" | $LOGIT

echo | $LOGIT

cd $SRC_LOC/calcentral

echo "`date`: Fetching new sources for CalCentral..." | $LOGIT
git pull >>$LOG 2>&1
echo "Last commit:" | $LOGIT
git log -1 | $LOGIT

echo | $LOGIT
echo "------------------------------------------" | $LOGIT
echo "Updating local configuration files..." | $LOGIT

CONFIG_FILES="$SRC_LOC/calcentral/launcher/properties"

# put the postgres password into config file
POSTGRES_CFG=$CONFIG_FILES/dataSource.properties
if [ -f $POSTGRES_CFG ]; then
	grep -v dataSource\.password= $POSTGRES_CFG > $POSTGRES_CFG.new
	echo "dataSource.password=\"$POSTGRES_PASSWORD\"" >> $POSTGRES_CFG.new
	mv -f $POSTGRES_CFG.new $POSTGRES_CFG
fi

cd launcher
echo "`date`: Stopping CalCentral..." | $LOGIT
mvn -B -e jetty:stop >>$LOG 2>&1 | $LOGIT

# initialize the db
echo "------------------------------------------" | $LOGIT
echo "Migrating the database..." | $LOGIT
cd ..
mvn -B -e flyway:migrate -Dflyway.password=$POSTGRES_PASSWORD >>$LOG 2>&1 | $LOGIT

echo "`date`: Starting CalCentral..." | $LOGIT
cd launcher
mkdir -p logs
mvn -B -e clean install >>$LOG 2>&1 | $LOGIT

# actually run the server (in the background)
nohup mvn -B -e jetty:run-war -DcustomPropsDir=$SRC_LOC/calcentral/launcher/properties >> logs/jetty.log 2>&1 &

# wait 30s for server to get started
sleep 30;

# do a GET to index.jsp so the Spring context will load up
curl -i http://localhost:8080/ | grep "HTTP/1.1 200 OK" || echo "ERROR: Index page failed to respond 200 OK" | $LOGIT

echo | $LOGIT
echo "`date`: Reinstall complete." | $LOGIT
