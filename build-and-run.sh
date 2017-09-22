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

if [ "$@" == "--bark" ]; then

    GITHUB_INTEGRATION_DIR=${SCRIPT_DIR}/github_integration

    #Build Bark
    cd {GITHUB_INTEGRATION_DIR} && ./gradlew clean build

    # Docker-compose
    cd ${SCRIPT_DIR} && docker-compose -f docker-compose.yaml -f docker-compose-bark.yaml up --build
else
    # Docker-compose
    cd ${SCRIPT_DIR} && docker-compose up --build
fi
