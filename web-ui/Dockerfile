# ---- Setup builder image -------------------------
FROM registry.opensource.zalan.do/stups/node:8.8.0-alpine-33 as yarn-image
WORKDIR /home/
RUN npm install -g yarn

# ---- Run npm build as image -------------------------
FROM yarn-image as builder
COPY ./ ./
RUN yarn && yarn build

# ---- Create production image -------------------------
FROM yarn-image
MAINTAINER "http://zalando.github.io/"
ENV NODE_ENV=production

COPY --from=builder /home/package.json /home/server.js /home/yarn.lock ./
COPY --from=builder /home/src/ ./src

RUN yarn --production

EXPOSE 3000

CMD ["node", "server.js"]
