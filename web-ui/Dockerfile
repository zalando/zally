FROM registry.opensource.zalan.do/stups/node:8.8.0-alpine-33

MAINTAINER "http://zalando.github.io/"

COPY package.json ./
COPY yarn.lock ./
COPY packages ./packages
COPY src ./src
COPY server.js ./

RUN npm install -g yarn
RUN cd packages/passport-oauth2-zalando && yarn install && yarn build
RUN yarn install --production

EXPOSE 3000

CMD ["node", "server.js"]
