# FROM openjdk:11
# MAINTAINER baeldung.com
# COPY target/stickers-0.0.1.jar app.jar
# ENTRYPOINT ["java","-jar","/app.jar"]

FROM openjdk:11-jdk-slim
ARG JAR_FILE=*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app.jar"]

