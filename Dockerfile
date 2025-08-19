FROM maven:3.8-eclipse-temurin-11 AS builder

WORKDIR /build

COPY pom.xml .

RUN mvn dependency:go-offline

COPY src ./src

RUN mvn package -DskipTests

FROM eclipse-temurin:11-jre-jammy

LABEL maintainer="Yustas Goosseff <goosseff8@gmail.com>"

RUN groupadd -r appgroup && useradd -r -g appgroup -s /bin/false appuser

WORKDIR /opt/app

RUN chown -R appuser:appgroup /opt/app

COPY --from=builder ./target/javaTlgBot-0.0.1-SNAPSHOT-jar-with-dependencies.jar app.jar

USER appuser

ENTRYPOINT ["java", "-jar", "app.jar"]