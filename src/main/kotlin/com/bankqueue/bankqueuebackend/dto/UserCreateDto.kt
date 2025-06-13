package com.bankqueue.bankqueuebackend.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UserCreateDto(
    @field:NotBlank(message = "Имя не может быть пустым")
    val name: String,

    @field:NotBlank(message = "Логин не может быть пустым")
    val login: String,

    @field:Email(message = "Неверный формат email")
    @field:NotBlank(message = "Email обязателен")
    val email: String,

    @field:NotBlank(message = "Пароль обязателен")
    @field:Size(min = 6, message = "Пароль минимум 6 символов")
    val password: String,

    @field:Pattern(
        regexp = """\+?[0-9]{10,15}""",
        message = "Номер должен содержать только цифры и может начинаться с '+', длина 10–15"
    )
    val phoneNumber: String
)
