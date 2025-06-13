package com.bankqueue.bankqueuebackend.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class UserUpdateDto(
    @field:NotBlank(message = "Имя не может быть пустым")
    val name: String? = null,

    @field:Email(message = "Неверный формат email")
    @field:NotBlank(message = "Email обязателен")
    val email: String? = null,

    @field:Pattern(
        regexp = """\+?[0-9]{10,15}""",
        message = "Номер должен содержать только цифры и может начинаться с '+', длина 10–15"
    )
    val phoneNumber: String? = null
)