#!/bin/bash
# script to update and then run calcentral

if [ -z "$1" ]; then
    echo "Usage: $0 source_root [logfile]"
    exit;
fi

SRC_LOC=$1
cd $SRC_LOC/calcentral

LOG=$2
if [ -z "$2" ]; then
    LOG=$SRC_LOC/calcentral/logs/update-restart.log
fi
LOGIT="tee -a $LOG"

echo "=========================================" | $LOGIT
echo "`date`: Updating CalCentral source code" | $LOGIT
git pull >>$LOG 2>&1
echo "Last commit:" | $LOGIT
git log -1 | $LOGIT
echo | $LOGIT\

./run.sh $1 $2
