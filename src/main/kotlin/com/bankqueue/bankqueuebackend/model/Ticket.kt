package com.bankqueue.bankqueuebackend.model

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "tickets")
data class Ticket(

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "user_id", nullable = false,
        foreignKey = ForeignKey(name = "fk_ticket_user")
    )
    var user: User,

    @Column(nullable = false)
    var address: String,

    @Column(name = "ticket_type", nullable = false)
    var ticketType: String,

    @Column(nullable = false)
    var ticket: String,

    @Column(name = "scheduled_at", nullable = false)
    var scheduledAt: OffsetDateTime

){
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}