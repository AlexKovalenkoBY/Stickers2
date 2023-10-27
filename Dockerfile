FROM openjdk:11
# MAINTAINER baeldung.com
COPY target/stickers-0.0.1.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
