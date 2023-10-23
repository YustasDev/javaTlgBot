FROM adoptopenjdk/openjdk11:alpine-jre  
MAINTAINER Yustas Goosseff <goosseff8@gmail.com>
ARG JAR_FILE=./target/javaTlgBot-0.0.1-SNAPSHOT-jar-with-dependencies.jar
WORKDIR /opt/app  
COPY ${JAR_FILE} app.jar  
ENTRYPOINT ["java","-jar","app.jar"] 