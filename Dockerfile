FROM eclipse-temurin:17-alpine
# FROM amazoncorretto:17-alpine
WORKDIR /workspace
COPY target/*.jar app.jar
EXPOSE 9966
ENTRYPOINT [ "java", "-jar", "/workspace/app.jar" ]