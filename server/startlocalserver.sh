#!/usr/bin/env bash

export SPRING_PROFILES_ACTIVE=dev
export SPRING_DATASOURCE_URL=jdbc:postgresql://postgres.local:5432/zally
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres
export MANAGEMENT_PORT=7979
export TOKEN_INFO_URI=https://url.not.set

docker run -d -p 5432:5432 --env POSTGRES_PASSWORD=postgres --env POSTGRES_DB=zally postgres:9.6
./gradlew clean
./gradlew bootRun


#Optional: Add an entry for `postgres.local` to /etc/hosts pointing to localhost
#echo "127.0.0.1  postgres.local" >> /etc/hosts


