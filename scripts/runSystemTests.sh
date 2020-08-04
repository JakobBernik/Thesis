#!/usr/bin/env bash

source $SCRIPTS_DIR/serviceScript.sh

#Test configured service that captures changes only on a single point (dp_42)
startServiceSimulator dp_42 true -1
robot --debug $ROBOT_DIR/reports/System/SystemTestsDebug.txt --output $ROBOT_DIR/reports/System/SystemTestsOutput.xml --log $ROBOT_DIR/reports/System/SystemTestsLog.html --report $ROBOT_DIR/reports/System/SystemTestsReport.html $ROBOT_DIR/04_SystemTests.robot
stopServiceSimulator