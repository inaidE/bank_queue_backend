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

2. Добавьте системные переменные `SPRING_DATASOURCE_PASSWORD`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_URL` в конфигурацию run

3. Запустите проект и проверьте его состояние в терминале через 
   ```bash
   http://localhost:8081/actuator/health
   ```