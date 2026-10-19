ARG BASE_IMAGE=eclipse-temurin:17
FROM ${BASE_IMAGE} as builder

WORKDIR /var/app

COPY . .
RUN ./gradlew
RUN ./gradlew build

FROM ${BASE_IMAGE}

LABEL org.opencontainers.image.authors="https://opensource.zalando.com/"

COPY zally-server/src/main/resources/api/zally-api.yaml /zalando-apis/zally-api.yaml
COPY --from=builder /var/app/zally-server/build/libs/zally-server.jar /

EXPOSE 8080

CMD java -jar /zally-server.jar
