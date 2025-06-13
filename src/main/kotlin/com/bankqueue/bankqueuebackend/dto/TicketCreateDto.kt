package com.bankqueue.bankqueuebackend.dto

import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotBlank
import java.time.OffsetDateTime

data class TicketCreateDto(
    @field:NotBlank(message = "Адрес не может быть пустым")
    val address: String,

    @field:NotBlank(message = "Тип талона не может быть пустым")
    val ticketType: String,

    @field:Future(message = "Время должно быть в будущем")
    val scheduledAt: OffsetDateTime
)