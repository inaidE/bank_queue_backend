package com.bankqueue.bankqueuebackend.dto

import java.time.OffsetDateTime
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class TicketCreateDto(
    @field:NotBlank(message = "address не может быть пустым")
    val address: String,

    @field:NotBlank(message = "ticketType не может быть пустым")
    val ticketType: String,

    @field:NotNull(message = "scheduledAt обязателен")
    val scheduledAt: OffsetDateTime
)