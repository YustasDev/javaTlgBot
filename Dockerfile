FROM adoptopenjdk/openjdk11:x86_64-ubuntu-jdk-11.0.28_6-slim AS builder

RUN apt-get update && apt-get install -y maven

WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package -DskipTests

FROM adoptopenjdk/openjdk11:alpine-jre

LABEL maintainer="Yustas Goosseff <goosseff8@gmail.com>"

RUN addgroup -S appgroup && adduser -S -G appgroup appuser

WORKDIR /opt/app

COPY --from=builder ./build/target/javaTlgBot-0.0.1-SNAPSHOT-jar-with-dependencies.jar app.jar

RUN chown -R appuser:appgroup /opt/app

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]