#!/bin/bash
# script to run when developing for Calcentral

pg_ctl -D /usr/local/var/postgres status > /dev/null 2>&1
if [ "$?" -gt "0" ]; then
	echo "PostgreSQL not running yet, we're starting it right now"
	pg_ctl -D /usr/local/var/postgres -l /usr/local/var/postgres/server.log start
else
	echo "PostgreSQL is already running"
fi

echo "Cleaning up the database and doing the migration"
mvn flyway:clean

echo "Building Calcentral and adding the Canvas Course Ids"
mvn clean install -Dmaven.test.skip=true -P load-canvas-course-ids exec:java -Dspring.profiles.active=default

echo "Starting the Jetty server"
mvn clean package jetty:run
