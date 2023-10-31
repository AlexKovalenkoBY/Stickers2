# FROM openjdk:11
# MAINTAINER baeldung.com
# COPY target/stickers-0.0.1.jar app.jar
# ENTRYPOINT ["java","-jar","/app.jar"]

# FROM openjdk:11-jdk-slim
# ARG JAR_FILE=*.jar
# COPY ${JAR_FILE} app.jar
# EXPOSE 8081
# ENTRYPOINT ["java", "-jar", "/app.jar"]

# FROM openjdk:8-jdk-alpine
# COPY --from=build /home/app/target/stickers-0.0.1.jar app.jar
# ENTRYPOINT ["java","-jar","/app.jar"]
FROM maven:3.8.3-openjdk-11-slim AS build
WORKDIR /
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:11-jre-slim
COPY --from=build /target/stickers-0.0.1.jar /myapp.jar
EXPOSE 8081
CMD ["java", "-jar", "/myapp.jar"]