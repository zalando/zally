#
# Build the java application in a separate container.
#
FROM eclipse-temurin:17  as builder
COPY . /src
WORKDIR /src
RUN ./gradlew build

FROM eclipse-temurin:17

LABEL org.opencontainers.image.authors="https://opensource.zalando.com/"

COPY zally-server/src/main/resources/api/zally-api.yaml /zalando-apis/zally-api.yaml
COPY --from=builder /src/zally-server/build/libs/zally-server.jar /

EXPOSE 8080

CMD java $(java-dynamic-memory-opts) $(appdynamics-agent) -jar /zally-server.jar
