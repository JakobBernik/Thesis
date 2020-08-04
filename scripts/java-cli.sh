#!/usr/bin/env bash
#executes runner and prints exit code to file for robotFW to read and make a report
java -cp "target/*" $1 $2 $3
echo $? > $ROBOT_DIR/exitCode.txt
sleep 1