FROM debian:stretch

RUN apt-get -yy update && \
  apt-get -yy install ca-certificates && \
  rm -rf /var/lib/apt/lists/*

ENV ZALLY_URL https://zally-server.kube.int.corefiling.com/

ADD ./zally/zally /usr/bin/zally
RUN chmod +x /usr/bin/zally
