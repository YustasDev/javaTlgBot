FROM adoptopenjdk/openjdk11:alpine-jre  
MAINTAINER Yustas Goosseff <goosseff8@gmail.com>
ARG JAR_FILE=./target/javaTlgBot-0.0.1-SNAPSHOT-jar-with-dependencies.jar
WORKDIR /opt/app  
COPY ${JAR_FILE} app.jar  
COPY ./Ein2.jpg Ein2.jpg
ENTRYPOINT ["java","-jar","app.jar"] 