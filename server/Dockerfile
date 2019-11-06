#
# Build the java application in a separate container.
#
FROM registry.opensource.zalan.do/stups/openjdk:latest as builder
COPY . /src
WORKDIR /src
RUN ./gradlew build --info


FROM registry.opensource.zalan.do/stups/openjdk:latest

MAINTAINER "http://zalando.github.io/"

COPY src/main/resources/api/zally-api.yaml /zalando-apis/zally-api.yaml
COPY --from=builder /src/build/libs/zally.jar /

EXPOSE 8080

CMD java $(java-dynamic-memory-opts) $(appdynamics-agent) -jar /zally.jar
