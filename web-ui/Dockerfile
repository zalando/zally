FROM registry.opensource.zalan.do/library/node-18-alpine:latest

MAINTAINER "http://zalando.github.io/"

RUN apk --no-cache add curl

COPY ./ ./
RUN yarn
RUN yarn build

EXPOSE 3000

CMD ["yarn", "start"]