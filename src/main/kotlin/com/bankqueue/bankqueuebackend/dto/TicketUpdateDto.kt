package com.bankqueue.bankqueuebackend.dto

import java.time.OffsetDateTime

data class TicketUpdateDto(
    val address: String? = null,
    val ticketType: String? = null,
    val scheduledAt: OffsetDateTime? = null
)