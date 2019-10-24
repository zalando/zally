FROM node:12

MAINTAINER "http://zalando.github.io/"

COPY package.json ./
COPY yarn.lock ./

RUN npm install -g yarn
RUN yarn --production

COPY src ./src
COPY server.js ./

EXPOSE 3000

CMD ["node", "server.js"]
