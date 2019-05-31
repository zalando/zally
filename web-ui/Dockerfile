FROM registry.opensource.zalan.do/stups/node:10.15.3-45

MAINTAINER "http://zalando.github.io/"

COPY package.json ./
COPY yarn.lock ./

RUN npm install -g yarn
RUN yarn --production

COPY src ./src
COPY server.js ./

EXPOSE 3000

CMD ["node", "server.js"]
