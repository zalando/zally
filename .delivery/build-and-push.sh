#!/usr/bin/env bash
# This CDP script builds and pushes Docker images

readonly DOCKER_HOST="pierone.stups.zalan.do"
readonly DOCKER_TEAM="architecture"
readonly DOCKER_VERSION="${CDP_TARGET_BRANCH}-${CDP_TARGET_COMMIT_ID:0:8}-${CDP_TARGET_REPOSITORY_COUNTER}"
readonly IS_PR_BUILD=${CDP_PULL_REQUEST_NUMBER+true}
readonly IS_CDP_BUILD=${CDP_TARGET_BRANCH+true}
readonly REPO_ROOT=$PWD

if [ "$IS_PR_BUILD" = true ]; then
  echo "We're in a pull request, aborting."
  exit 0
fi

# Get files which have been changed

changed_files=($(curl -s https://api.github.com/repos/zalando-incubator/zally/pulls/${CDP_PULL_REQUEST_NUMBER}/files | jq '.[] | .filename' | tr -d "\""))

server_changed=false
web_ui_changed=false

for f in "${changed_files[@]}"
do
  if [[ $f == server/* ]]; then
    server_changed=true
  fi
  if [[ $f == web-ui/* ]]; then
    web_ui_changed=true
  fi
done

# Build and push Docker images

if [ "$server_changed" = true ]; then
  echo "Building and pushing Zally Server"
  cd ${REPO_ROOT}/server
  docker build -t "${DOCKER_HOST}/${DOCKER_TEAM}/zally:${DOCKER_VERSION}" .
  docker push "${DOCKER_HOST}/${DOCKER_TEAM}/zally:${DOCKER_VERSION}"
fi

if [ "$web_ui_changed" = true ]; then
  echo "Building and pushing Zally Web UI Dummy"
  cd ${REPO_ROOT}/web-ui
  docker build -t "${DOCKER_HOST}/${DOCKER_TEAM}/zally-web-ui-dummy:${DOCKER_VERSION}" .
  docker push "${DOCKER_HOST}/${DOCKER_TEAM}/zally-web-ui-dummy:${DOCKER_VERSION}"
fi