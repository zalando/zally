FROM registry.opensource.zalan.do/library/node-18-alpine:latest AS builder

MAINTAINER "http://zalando.github.io/"

WORKDIR /app

COPY package.json yarn.lock ./
RUN yarn install --frozen-lockfile

COPY src ./src
COPY server.js ./
COPY webpack ./webpack
COPY .babelrc ./

RUN yarn build

FROM registry.opensource.zalan.do/library/node-18-alpine:latest AS runtime

WORKDIR /app

RUN apk --no-cache add curl

COPY package.json yarn.lock ./
RUN yarn install --production --frozen-lockfile

COPY --from=builder /app/src ./src
COPY --from=builder /app/server.js ./

EXPOSE 3000

CMD ["node", "server.js"]
