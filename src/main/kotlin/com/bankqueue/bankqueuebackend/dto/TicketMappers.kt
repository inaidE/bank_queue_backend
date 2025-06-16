package com.bankqueue.bankqueuebackend.dto

import com.bankqueue.bankqueuebackend.model.Ticket

/** Entity -> DTO */
fun Ticket.toResponseDto() = TicketResponseDto(
    id = this.id,
    userId = this.user.id!!,
    address = this.address,
    ticketType = this.ticketType,
    ticket = this.ticket,
    scheduledAt = this.scheduledAt
)
