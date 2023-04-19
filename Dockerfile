FROM --platform=linux/amd64 openjdk:19-alpine3.16

MAINTAINER viet.mike

WORKDIR /app

COPY .mvn ./.mvn

COPY src ./src

COPY mvnw pom.xml ./

RUN chmod +x mvnw

ENTRYPOINT ["./mvnw", "spring-boot:run", "-Pprod"]