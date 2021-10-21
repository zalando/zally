FROM registry.opensource.zalan.do/library/node-14-alpine:latest

MAINTAINER "http://zalando.github.io/"

COPY package.json ./
COPY yarn.lock ./

RUN yarn --production

COPY src ./src
COPY server.js ./

EXPOSE 3000

CMD ["node", "server.js"]
