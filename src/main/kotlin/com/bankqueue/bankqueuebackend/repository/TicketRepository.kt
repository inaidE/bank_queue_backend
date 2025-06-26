package com.bankqueue.bankqueuebackend.repository

import com.bankqueue.bankqueuebackend.model.Ticket
import com.bankqueue.bankqueuebackend.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
interface TicketRepository : JpaRepository<Ticket, Long> {
    fun findAllByUserId(userId: Long): List<Ticket>
    fun findAllByUser(user: User): List<Ticket>
    fun findByIdAndUserLogin(id: Long, login: String): Ticket?
    fun existsByAddressAndScheduledAt(address: String, scheduledAt: OffsetDateTime): Boolean
    fun existsByAddressAndScheduledAtAndIdNot(
        address: String,
        scheduledAt: OffsetDateTime,
        id: Long
    ): Boolean

    @Query("SELECT MAX(t.ticket) FROM Ticket t WHERE t.ticketType = :type")
    fun findMaxTicketCodeByType(@Param("type") type: String): String?
}
