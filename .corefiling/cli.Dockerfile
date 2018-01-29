FROM debian:stretch

RUN apt-get -yy update && \
  apt-get -yy install ca-certificates curl jq && \
  rm -rf /var/lib/apt/lists/*

ARG ZALLY_GITLAB_TOKEN
ENV ZALLY_GITLAB_TOKEN ${ZALLY_GITLAB_TOKEN}
ENV ZALLY_URL https://zally-server.kube.int.corefiling.com/

ADD ./zally/zally /usr/bin/zally
ADD ./zally/cflzally.sh /usr/bin/cflzally
RUN chmod +x /usr/bin/zally /usr/bin/cflzally
