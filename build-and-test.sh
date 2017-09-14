#!/usr/bin/env bash
# This script builds all the components and runs unit and integration tests

set -ex

pushd $(dirname $0) > /dev/null
SCRIPT_DIR=$(pwd -P)
popd > /dev/null
#ZALLY_GO_PATH="${GOPATH}/src/github.com/zalando-incubator/zally"

# Postgres needs a non-root user to init a database
#adduser --disabled-password --gecos "" user
#chown -R user:user ${SCRIPT_DIR}

# Unit-test and build server
cd ${SCRIPT_DIR}/server/
./gradlew build --info

## Unit-test and build GHE integration server
#cd ${SCRIPT_DIR}/github-integration/
#./gradlew build --info
#
## Launch local zally server
#cd ${SCRIPT_DIR}/server/
#./gradlew bootRun > /dev/null &
#echo $! > /tmp/zally_server.pid
#
## Wait until Spring Boot will start
#while ! printf "GET / HTTP/1.0\n" | nc localhost 8080; do sleep 1; done
#
## Test and build CLI
#mkdir -p ${ZALLY_GO_PATH}
#cp -R . ${ZALLY_GO_PATH}
#cd ${ZALLY_GO_PATH}/cli/zally
#go get -v -t -tags=integration
#./test.sh integration
#go build
#
## Kill Zally server instance
#kill -9 $(cat /tmp/zally_server.pid)
#
## Unit-test web UI
#cd web-ui
#node --version
#npm --version
#npm install -q
#npm test
#npm run build
#
## Push test coverage
#bash <(curl -s https://codecov.io/bash) -f server/build/reports/jacoco/test/jacocoTestReport.xml
#bash <(curl -s https://codecov.io/bash) -f github-integration/build/reports/jacoco/test/jacocoTestReport.xml
#bash <(curl -s https://codecov.io/bash) -f ${ZALLY_GO_PATH}/cli/zally/coverage.txt -f ${SCRIPT_DIR}/web-ui/target/coverage/lcov.info