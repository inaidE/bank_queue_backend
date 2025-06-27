# Backend (BankQueue)

Это серверный сервис на Kotlin + Spring Boot, реализующий REST API для банковской очереди.

## Что внутри

- `src/main/kotlin` — код приложения (модели, сервисы, контроллеры).
- `src/main/resources` — конфигурации (`application.yml`) и миграции Flyway.
- `Dockerfile` — инструкция для сборки Docker-образа.

## Локальная разработка без Docker

1. Склонируйте репозиторий:
   ```bash
   https://github.com/inaidE/bank_queue_backend.git
   ```
   
2. Проект выполнен на JDK 17 (v. 17.0.15)(ms-17)

3. Добавьте системные переменные `SPRING_DATASOURCE_PASSWORD`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_URL`,
`SECURITY_USERNAME`, `SECURITY_PASSWORD` в конфигурацию run

4. Запустите проект и проверьте его состояние в терминале через 
   ```bash
   http://localhost:8081/actuator/health
   ```
5. Для тестирования через `swagger-ui` http://localhost:8081/swagger-ui/index.html

6. Для тестирования через JUnit тесты
   ```bash
   ./gradlew test
   ```
