#!/usr/bin/env bash
# This CDP script builds all the components and runs unit and integration tests

set -ex

pushd $(dirname $0) > /dev/null
SCRIPT_DIR=$(pwd -P)
popd > /dev/null

readonly DOCKER_HOST="pierone.stups.zalan.do"
readonly DOCKER_TEAM="architecture"
readonly DOCKER_VERSION="${CDP_TARGET_BRANCH}-${CDP_TARGET_COMMIT_ID:0:8}-${CDP_TARGET_REPOSITORY_COUNTER}"
readonly IS_PR_BUILD=${CDP_PULL_REQUEST_NUMBER+true}

readonly USER_HOME="/user"
readonly ENV_FILE="${USER_HOME}/cdp.env"
readonly GOPATH="${USER_HOME}/go"
readonly ZALLY_GO_PATH="${GOPATH}/src/github.com/zalando-incubator/zally"

mkdir -p ${ZALLY_GO_PATH}

# Setup a user for tests
adduser --disabled-password --home ${USER_HOME} --gecos "" user
usermod -aG sudo user
echo "user ALL=(ALL) NOPASSWD:ALL" >> /etc/sudoers
chown -R user:user ${SCRIPT_DIR}
chown -R user:user ${ZALLY_GO_PATH}
chown -R user:user ${USER_HOME}

echo "SCRIPT_DIR=${SCRIPT_DIR}" >> ${ENV_FILE}
echo "GOPATH=${GOPATH}" >> ${ENV_FILE}
echo "ZALLY_GO_PATH=${ZALLY_GO_PATH}" >> ${ENV_FILE}

su - user
mkdir ~/bin/
source /user/cdp.env

# Unit-test and build Zally Server
cd ${SCRIPT_DIR}/server/
./gradlew build --info

# Unit-test and build GHE Integration Server
cd ${SCRIPT_DIR}/github-integration
./gradlew build --info

# Launch local Zally Server
cd ${SCRIPT_DIR}/server/
./gradlew bootRun > /dev/null &
echo $! > ~/zally_server.pid

# Wait until Spring Boot will start
while ! printf "GET / HTTP/1.0\n" | nc -vz localhost 8080; do sleep 1; done

# Test and build Zally CLI
curl -sL -o ~/bin/gimme https://raw.githubusercontent.com/travis-ci/gimme/master/gimme
chmod +x ~/bin/gimme
eval "$(GIMME_GO_VERSION=1.8.3 ~/bin/gimme)"

cd ${SCRIPT_DIR}
cp -R . ${ZALLY_GO_PATH}
cd ${ZALLY_GO_PATH}/cli/zally
go get -v -t -tags=integration
./test.sh integration
go build

# Kill Zally server instance
kill -9 $(cat ~/zally_server.pid)

# Unit-test web UI
curl -o- https://raw.githubusercontent.com/creationix/nvm/v0.33.4/install.sh | bash
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && . "$NVM_DIR/nvm.sh"
nvm install 7

cd ${SCRIPT_DIR}/web-ui
npm install -q
npm test
npm run build

# Push test coverage
bash <(curl -s https://codecov.io/bash) -f ${SCRIPT_DIR}/server/build/reports/jacoco/test/jacocoTestReport.xml
bash <(curl -s https://codecov.io/bash) -f ${SCRIPT_DIR}/github-integration/build/reports/jacoco/test/jacocoTestReport.xml
bash <(curl -s https://codecov.io/bash) -f ${ZALLY_GO_PATH}/cli/zally/coverage.txt -f ${SCRIPT_DIR}/web-ui/target/coverage/lcov.info

exit

if [ "$IS_PR_BUILD" = true ]; then
  echo "We're in a pull request, aborting."
  exit 0
fi

# Build and push GHE Integration Docker image
cd ${SCRIPT_DIR}/github-integration
docker build -t "${DOCKER_HOST}/${DOCKER_TEAM}/ghe-integration:${DOCKER_VERSION}" .
docker push "${DOCKER_HOST}/${DOCKER_TEAM}/ghe-integration:${DOCKER_VERSION}"

# Build and push Zally Server Docker image
cd ${SCRIPT_DIR}/server
docker build -t "${DOCKER_HOST}/${DOCKER_TEAM}/zally:${DOCKER_VERSION}" .
docker push "${DOCKER_HOST}/${DOCKER_TEAM}/zally:${DOCKER_VERSION}"

# Build and push Zally Web UI dummy image
cd ${SCRIPT_DIR}/web-ui
docker build -t "${DOCKER_HOST}/${DOCKER_TEAM}/zally-web-ui-dummy:${DOCKER_VERSION}" .
docker push "${DOCKER_HOST}/${DOCKER_TEAM}/zally-web-ui-dummy:${DOCKER_VERSION}"