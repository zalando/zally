FROM registry.opensource.zalan.do/stups/openjdk:latest

LABEL MAINTAINER "http://zalando.github.io/"

COPY build/libs/zally-gihub-integration-1.0.0.jar /
COPY scm-source.json /scm-source.json

EXPOSE 8080

CMD java $(java-dynamic-memory-opts) $(appdynamics-agent) -jar /zally-github-integration-1.0.0.jar
