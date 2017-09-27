FROM artifacts.int.corefiling.com:5000/cfl-node:8-alpine

ENV SASS_BINARY_URL "https://github.com/sass/node-sass/releases/download/v4.5.3/linux-x64-57_binding.node"
ENV SASS_BINARY_PATH "/opt/sass/binding.node"

RUN mkdir -p "/opt/sass/"
RUN apk add --no-cache curl && curl -Lo "$SASS_BINARY_PATH" "$SASS_BINARY_URL" && apk del curl
