#!/usr/bin/env bash

source $SCRIPTS_DIR/serviceScript.sh

startServiceSimulator
robot --debug $ROBOT_DIR/reports/OldbAPI/OldbAPITestsDebug.txt --output $ROBOT_DIR/reports/OldbAPI/OldbAPITestsOutput.xml --log $ROBOT_DIR/reports/OldbAPI/OldbAPITestsLog.html --report $ROBOT_DIR/reports/OldbAPI/OldbAPITestsReport.html $ROBOT_DIR/03_OldbAPITests.robot
stopServiceSimulator