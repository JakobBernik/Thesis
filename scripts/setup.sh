#!/usr/bin/env bash

#Setup variables and create clean package of a project. Tests are skipped
export ROOT_DIR=$(pwd)
export ROBOT_DIR=$(cd robot/;pwd)
export SCRIPTS_DIR=$(cd scripts/;pwd)
export PATH=$PATH:$SCRIPTS_DIR

mvn clean package -DskipTests
