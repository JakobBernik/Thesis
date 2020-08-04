#!/usr/bin/env bash

#Start service in background
startServiceSimulator() {
  java -cp 'target/*' thesis.testing.system.ServiceSimulator "$@" &
}

#find and terminate service process
stopServiceSimulator() {
 ps | grep "$(jps | awk '/[S]erviceSimulator/{print $1}')" | awk '{print $1}' | xargs --no-run-if-empty kill -9
}