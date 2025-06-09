package com.bankqueue.bankqueuebackend.dto

import java.time.OffsetDateTime

data class TicketResponseDto(
    val id: Long?,
    val userId: Long,
    val address: String,
    val ticketType: String,
    val ticket: String,
    val scheduledAt: OffsetDateTime
)