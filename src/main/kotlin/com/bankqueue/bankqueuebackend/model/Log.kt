package com.bankqueue.bankqueuebackend.model

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime

@Entity
@Table(name = "logs")
open class Log(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_log_user")
    )
    var user: User,

    @Column(name = "event_type", nullable = false)
    var eventType: String,

    @Column(name = "event_time", nullable = false)
    var eventTime: OffsetDateTime,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var details: Map<String, Any>? = null

)
