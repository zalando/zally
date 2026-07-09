FROM eclipse-temurin:17 as builder

WORKDIR /var/app

COPY . .
RUN ./gradlew
RUN ./gradlew build

FROM eclipse-temurin:17

LABEL org.opencontainers.image.authors="https://opensource.zalando.com/"

COPY zally-server/src/main/resources/api/zally-api.yaml /zalando-apis/zally-api.yaml
COPY --from=builder /var/app/zally-server/build/libs/zally-server.jar /

EXPOSE 8080

CMD java -Dbind-type=true -jar /zally-server.jar
