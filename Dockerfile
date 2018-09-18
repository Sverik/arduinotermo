FROM openjdk:8-alpine as build

RUN apk update && apk add bash

WORKDIR /src/

ADD build.gradle ./
ADD gradle ./gradle
ADD gradlew ./

RUN ./gradlew clean

ADD . /src/

RUN ./gradlew generateBrewdataJooqSchemaSource -Ph2init="INIT=runscript from '/src/src/main/resources/scripts/h2_schema.sql'"

RUN ./gradlew bootRepackage -Ph2init="INIT=runscript from '/src/src/main/resources/scripts/h2_schema.sql'"

FROM openjdk:8-jre-alpine

COPY --from=build /src/build/libs/brewmon-0.0.6-SNAPSHOT.jar /app.jar

WORKDIR /

CMD java -jar /app.jar
