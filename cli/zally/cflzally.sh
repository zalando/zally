#!/usr/bin/env bash

PATH=$1
JSONTOMARKDOWN=".violations[] | \"* **[\(.violation_type) \(.title)](\(.rule_link))**\", \"  * \(.description)\", (.paths[] | \"  * \(.)\")"

echo "Linting $PATH..."
/usr/bin/zally lint --json $PATH > /tmp/zally.json
RESULT=$?

echo "Formatting markdown comment..."
if /usr/bin/jq --exit-status ".violations | length > 0" /tmp/zally.json; then
    /usr/bin/jq \
        --raw-output \
        "$JSONTOMARKDOWN" \
        /tmp/zally.json > /tmp/zally.md
    /bin/sed -e s/^/\ \ /g /tmp/zally.md

    echo "Commenting violations on gitlab commit..."
    /usr/bin/curl \
        --silent \
        --request POST \
        --header "PRIVATE-TOKEN: ${ZALLY_GITLAB_TOKEN}" \
        --form "note=</tmp/zally.md" \
        --form "path=$PATH" \
        --form "line=1" \
        --form "line_type=new" \
        ${CI_PROJECT_URL}/../../api/v4/projects/${CI_PROJECT_ID}/repository/commits/${CI_BUILD_REF}/comments
    
else
    echo "Commenting success on gitlab commit..."
    /usr/bin/curl \
        --silent \
        --request POST \
        --header "PRIVATE-TOKEN: ${ZALLY_GITLAB_TOKEN}" \
        --data "note=%F0%9F%91%8D" \
        --data "path=$PATH" \
        --data "line=1" \
        --data "line_type=new" \
        ${CI_PROJECT_URL}/../../api/v4/projects/${CI_PROJECT_ID}/repository/commits/${CI_BUILD_REF}/comments
fi

exit $RESULT