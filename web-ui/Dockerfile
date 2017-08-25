FROM registry.opensource.zalan.do/stups/node:8.2.0-alpine

MAINTAINER "http://zalando.github.io/"

COPY package.json ./
RUN npm install --production

COPY src ./src
COPY server.js ./

EXPOSE 3000

CMD ["node", "server.js"]
