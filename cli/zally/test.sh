#!/usr/bin/env bash

GO_PACKAGES=$(go list ./... | grep -v vendor)
RUN_TESTS="go test -coverprofile=cover.out -covermode=atomic -v"

if [ "$1" == "integration" ]; then
    RUN_TESTS="$RUN_TESTS -tags=integration"
fi

for package in $GO_PACKAGES; do
    if ! $RUN_TESTS $package ; then
        [ -e cover.out ] && rm cover.out
        echo "============================"
        echo "CLI Tests Failed. Exiting..."
        echo "============================"
        exit 1
    fi
    cat cover.out >> coverage.txt
done
rm cover.out

echo "======================"
echo " Code Coverage Report "
echo "======================"
cat coverage.txt
