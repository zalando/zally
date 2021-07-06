FROM registry.opensource.zalan.do/library/openjdk-11-jdk-slim:latest

MAINTAINER "http://zalando.github.io/"
LABEL MAINTAINER "http://zalando.github.io/"

COPY build/libs/zally-github-integration.jar /
COPY scm-source.json /scm-source.json

EXPOSE 8080

CMD java $(java-dynamic-memory-opts) $(appdynamics-agent) -jar /zally-github-integration.jar
