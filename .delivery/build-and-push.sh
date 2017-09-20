#!/usr/bin/env bash
# This CDP script builds all the components and runs unit and integration tests

readonly DOCKER_HOST="pierone.stups.zalan.do"
readonly DOCKER_TEAM="architecture"
readonly DOCKER_VERSION="${CDP_TARGET_BRANCH}-${CDP_TARGET_COMMIT_ID:0:8}-${CDP_TARGET_REPOSITORY_COUNTER}"
readonly IS_PR_BUILD=${CDP_PULL_REQUEST_NUMBER+true}
readonly IS_CDP_BUILD=${CDP_TARGET_BRANCH+true}

REPO_ROOT=$PWD

if [ "$IS_PR_BUILD" = true ]; then
  echo "We're in a pull request, aborting."
  exit 0
fi

#TODO Build docker images only of the components which were changed

# Build and push Zally Server Docker image
cd ${REPO_ROOT}/server
docker build -t "${DOCKER_HOST}/${DOCKER_TEAM}/zally:${DOCKER_VERSION}" .
docker push "${DOCKER_HOST}/${DOCKER_TEAM}/zally:${DOCKER_VERSION}"

# Build and push Zally Web UI dummy image
cd ${REPO_ROOT}/web-ui
docker build -t "${DOCKER_HOST}/${DOCKER_TEAM}/zally-web-ui-dummy:${DOCKER_VERSION}" .
docker push "${DOCKER_HOST}/${DOCKER_TEAM}/zally-web-ui-dummy:${DOCKER_VERSION}"