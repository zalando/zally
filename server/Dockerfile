FROM eclipse-temurin:17

LABEL org.opencontainers.image.authors="https://opensource.zalando.com/"

COPY zally-server/src/main/resources/api/zally-api.yaml /zalando-apis/zally-api.yaml
COPY zally-server/build/libs/zally-server.jar /

EXPOSE 8080

CMD java -jar /zally-server.jar
