#
# Build the java application in a separate container.
#
FROM registry.opensource.zalan.do/library/openjdk-11-jdk-slim:latest as builder
COPY . /src
WORKDIR /src
RUN ./gradlew build

FROM registry.opensource.zalan.do/library/openjdk-11-jdk-slim:latest

MAINTAINER "http://zalando.github.io/"

COPY zally-server/src/main/resources/api/zally-api.yaml /zalando-apis/zally-api.yaml
COPY --from=builder /src/zally-server/build/libs/zally-server.jar /

EXPOSE 8080

CMD java -jar /zally-server.jar
