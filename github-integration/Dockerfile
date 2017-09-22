FROM registry.opensource.zalan.do/stups/openjdk:1.8.0-131-8

MAINTAINER "http://zalando.github.io/"
LABEL MAINTAINER "http://zalando.github.io/"

COPY build/libs/bark.jar /
COPY scm-source.json /scm-source.json

EXPOSE 8080

CMD java $(java-dynamic-memory-opts) $(appdynamics-agent) -jar /bark.jar
