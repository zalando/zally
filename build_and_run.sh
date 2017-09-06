#!/usr/bin/env bash
# This script builds Zally server and Zally Web UI and starts the components via docker-compose

set -ex

pushd $(dirname $0) > /dev/null
SCRIPT_DIR=$(pwd -P)
popd > /dev/null

SERVER_DIR=${SCRIPT_DIR}/server
WEB_UI_DIR=${SCRIPT_DIR}/web-ui

# Build server
cd ${SERVER_DIR} && ./gradlew clean build

# Build web ui
cd ${WEB_UI_DIR} && npm install && npm build

# Docker-compose
cd ${SCRIPT_DIR} && docker-compose up --build
