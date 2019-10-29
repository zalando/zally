#!/usr/bin/env bash
# This script starts the Zally components via docker-compose:
#  the server component is separately built inside its own container.


set -ex

hash yarn 2>/dev/null || { echo >&2 "yarn is not installed.."; exit 1; }

pushd $(dirname $0) > /dev/null
SCRIPT_DIR=$(pwd -P)
popd > /dev/null

SERVER_DIR=${SCRIPT_DIR}/server
WEB_UI_DIR=${SCRIPT_DIR}/web-ui
GITHUB_INTEGRATION_DIR=${SCRIPT_DIR}/github_integration

# Build web ui
cd ${WEB_UI_DIR} && yarn && yarn build

if [[ "$@" == "--bark" ]]; then

    #Build Bark
    cd ${GITHUB_INTEGRATION_DIR} && ./gradlew build -x test

    # Docker-compose
    cd ${SCRIPT_DIR} && docker-compose -f docker-compose.yaml -f docker-compose-bark.yaml up --build
else
    # Docker-compose
    cd ${SCRIPT_DIR} && docker-compose up --build
fi
