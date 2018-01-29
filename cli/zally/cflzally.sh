#!/usr/bin/env bash

PATH=$1
JSONTOMARKDOWN=".violations[] | \"* **[\(.violation_type) \(.title)](\(.rule_link))**\", \"  * \(.description)\", (.paths[] | \"  * \(.)\")"

echo "Linting $PATH..."
/usr/bin/zally lint --json $PATH > /tmp/zally.json
RESULT=$?

if /usr/bin/jq --exit-status ".violations | length > 0" /tmp/zally.json; then

    echo "Formatting markdown comment..."
    /usr/bin/jq \
        --raw-output \
        "$JSONTOMARKDOWN" \
        /tmp/zally.json > /tmp/zally.md
    /bin/sed -e s/^/\ \ /g /tmp/zally.md

    echo "Commenting on gitlab commit..."
    /usr/bin/curl \
        --silent \
        --request POST \
        --header "PRIVATE-TOKEN: ${ZALLY_GITLAB_TOKEN}" \
        --form "note=</tmp/zally.md" \
        --form "path=$PATH" \
        --form "line=1" \
        --form "line_type=new" \
        ${CI_PROJECT_URL}/../../api/v4/projects/${CI_PROJECT_ID}/repository/commits/${CI_BUILD_REF}/comments
fi

exit $RESULT