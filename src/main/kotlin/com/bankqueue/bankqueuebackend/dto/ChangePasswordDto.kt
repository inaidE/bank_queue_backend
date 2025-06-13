package com.bankqueue.bankqueuebackend.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * DTO для смены пароля.
 * Пользователь присылает старый и новый (с подтверждением).
 */
data class ChangePasswordDto(
    @field:NotBlank(message = "Текущий пароль обязателен")
    val currentPassword: String,

    @field:NotBlank(message = "Новый пароль обязателен")
    @field:Size(min = 6, message = "Новый пароль минимум 6 символов")
    val newPassword: String,

    @field:NotBlank(message = "Подтверждение пароля обязательно")
    val confirmPassword: String
)