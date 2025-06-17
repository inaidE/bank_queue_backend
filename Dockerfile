# Stage 1: Сборка Spring Boot-приложения через Gradle Wrapper
FROM gradle:7.5-jdk17 AS build

# 1) Переключаемся на root, чтобы настроить кеш и права
USER root

# 2) Создаём папку для кеша Gradle и передаём её gradle:gradle
RUN mkdir -p /gradle-cache \
    && chown -R gradle:gradle /gradle-cache

# 3) Указываем Gradle использовать /gradle-cache
ENV GRADLE_USER_HOME=/gradle-cache

# 4) Возвращаемся к пользователю gradle
USER gradle

# 5) Рабочая директория в контейнере
WORKDIR /app

# 6) Копируем Gradle Wrapper и сразу делаем исполняемым
#    (COPY с --chown, чтобы скрипт имел владельца gradle:gradle)
COPY --chown=gradle:gradle gradlew gradlew.bat ./
RUN chmod +x gradlew

# 7) Копируем папку gradle/wrapper (необходима для работы gradlew)
COPY --chown=gradle:gradle gradle ./gradle

# 8) Копируем build-скрипты и исходники, назначая gradle:gradle
COPY --chown=gradle:gradle build.gradle.kts settings.gradle.kts ./
COPY --chown=gradle:gradle gradle.properties ./
COPY --chown=gradle:gradle src ./src

# 9) Запускаем сборку через Gradle Wrapper
#    Здесь gradlew автоматически подтянет нужную версию Gradle, указанную в gradle-wrapper.properties
RUN ./gradlew clean bootJar --no-daemon

# Stage 2: Запуск собранного JAR в лёгком образе
FROM openjdk:17-jdk-slim

# 10) Открываем порт 8080
EXPOSE 8080

# 11) Работаем в /app
WORKDIR /app

# 12) Копируем готовый JAR из build-этапа
COPY --from=build /app/build/libs/*.jar app.jar

# 13) Указываем команду запуска
ENTRYPOINT ["java", "-jar", "app.jar"]
