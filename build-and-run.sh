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

# Build web ui
cd ${WEB_UI_DIR} && yarn && yarn build

# Docker-compose

cd ${SCRIPT_DIR} && docker compose build
ZALLY_API_URL=http://34.235.128.99 docker compose up -d
