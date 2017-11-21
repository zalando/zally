#!/usr/bin/env bash

variant="$1"
if [ -z "$variant" ]; then
  echo "Usage: $0 variant" 1>&2
  exit 1
fi

cd "$( dirname "${BASH_SOURCE[0]}" )"

project_path="pdds/zally"
image="artifacts.int.corefiling.com:5000/$project_path/$1:local"
dockerfile="$variant.Dockerfile"

echo "Building $image from $dockerfile"
docker build -t "$image" -f "$dockerfile" . || exit $?

echo "Pushing $image"
docker push "$image" || exit $?
