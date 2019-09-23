#!/usr/bin/env bash
# This CDP script builds and pushes Docker images

set -ex

env

readonly DOCKER_HOST="pierone.stups.zalan.do"
readonly DOCKER_TEAM="pitchfork"
readonly IS_PR_BUILD=${CDP_PULL_REQUEST_NUMBER+true}
readonly IS_CDP_BUILD=${CDP_TARGET_BRANCH+true}
readonly REPO_ROOT=$PWD
readonly DOCKER_VERSION="${CDP_TARGET_BRANCH}-${CDP_TARGET_COMMIT_ID:0:8}-${CDP_TARGET_REPOSITORY_COUNTER}"

if [ "$IS_PR_BUILD" = true ]; then
  echo "We're in a pull request, aborting."
  exit 0
fi

# Get files which have been changed
pr_number=$(curl -s https://api.github.com/repos/zalando/zally/git/commits/${CDP_TARGET_COMMIT_ID} | jq '.message' | sed 's/.* #\([0-9]*\)\+ .*/\1/')
changed_files=($(curl -s https://api.github.com/repos/zalando/zally/pulls/${pr_number}/files | jq '.[] | .filename' | tr -d "\""))

server_changed=false
web_ui_changed=false
ghe_integration_changed=false

for f in "${changed_files[@]}"
do
  if [[ $f == server/* ]]; then
    server_changed=true
  fi
  if [[ $f == web-ui/* ]]; then
    web_ui_changed=true
  fi
  if [[ $f == github-integration/* ]]; then
    ghe_integration_changed=true
  fi
  if [[ $f == .delivery/build-and-push.sh ]]; then
    server_changed=true
    web_ui_changed=true
    ghe_integration_changed=true
  fi
done

# Build and push Docker images

if [ "$server_changed" = true ]; then
  echo "Building and pushing Zally Server"
  cd ${REPO_ROOT}/server
  docker build -t "${DOCKER_HOST}/${DOCKER_TEAM}/zally:${DOCKER_VERSION}" .
  docker push "${DOCKER_HOST}/${DOCKER_TEAM}/zally:${DOCKER_VERSION}"
  echo "Zally Server has been built and pushed"
fi

if [ "$web_ui_changed" = true ]; then
  echo "Building and pushing Zally Web UI Dummy"
  cd ${REPO_ROOT}/web-ui
  docker build -t "${DOCKER_HOST}/${DOCKER_TEAM}/zally-web-ui-dummy:${DOCKER_VERSION}" .
  docker push "${DOCKER_HOST}/${DOCKER_TEAM}/zally-web-ui-dummy:${DOCKER_VERSION}"
  echo "Zally Web UI has been built and pushed"
fi

if [ "$ghe_integration_changed" = true ]; then
  echo "Building and pushing Zally Github Integration"
  cd ${REPO_ROOT}/github-integration
  docker build -t "${DOCKER_HOST}/${DOCKER_TEAM}/zally-github-integration:${DOCKER_VERSION}" .
  docker push "${DOCKER_HOST}/${DOCKER_TEAM}/zally-github-integration:${DOCKER_VERSION}"
  echo "Zally Github Integration has been built and pushed"
fi