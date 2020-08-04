#!/usr/bin/env bash

source $SCRIPTS_DIR/serviceScript.sh

startServiceSimulator
robot --debug $ROBOT_DIR/reports/ArchivingAPI/ArchivingAPITestsDebug.txt --output $ROBOT_DIR/reports/ArchivingAPI/ArchivingAPITestsOutput.xml --log $ROBOT_DIR/reports/ArchivingAPI/ArchivingAPITestsLog.html --report $ROBOT_DIR/reports/ArchivingAPI/ArchivingAPITestsReport.html $ROBOT_DIR/01_ArchivingAPITests.robot
stopServiceSimulator