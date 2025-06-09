package com.bankqueue.bankqueuebackend.repository

import com.bankqueue.bankqueuebackend.model.Ticket
import com.bankqueue.bankqueuebackend.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TicketRepository : JpaRepository<Ticket, Long> {
    fun findAllByUserId(userId: Long): List<Ticket>
    fun findAllByUser(user: User): List<Ticket>
}
