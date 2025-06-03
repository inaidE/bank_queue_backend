# Stage 1: build JAR
FROM gradle:7.5-jdk17 AS build
WORKDIR /app
COPY --chown=gradle:gradle build.gradle.kts settings.gradle.kts ./
COPY --chown=gradle:gradle src ./src
RUN gradle clean bootJar --no-daemon

# Stage 2: final lightweight image
FROM openjdk:17-jdk-slim
EXPOSE 8080
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]