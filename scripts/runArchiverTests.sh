#!/usr/bin/env bash

source $SCRIPTS_DIR/serviceScript.sh

startServiceSimulator
robot --debug $ROBOT_DIR/reports/Archiver/ArchiverTestsDebug.txt --output $ROBOT_DIR/reports/Archiver/ArchiverTestsOutput.xml --log $ROBOT_DIR/reports/Archiver/ArchiverTestsLog.html --report $ROBOT_DIR/reports/Archiver/ArchiverTestsReport.html $ROBOT_DIR/02_ArchiverTests.robot
stopServiceSimulator