package com.bankqueue.bankqueuebackend.model

import jakarta.persistence.*
import org.hibernate.annotations.BatchSize


@Entity
@Table(name = "users")
data class User (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, unique = true)
    var login: String,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(name = "encrypted_password", nullable = false)
    var encryptedPassword: String,

    @Column(name = "phone_number", nullable = false, unique = true)
    var phoneNumber: String
){
    @OneToMany(
        mappedBy = "user",
        cascade = [(CascadeType.ALL)],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @BatchSize(size = 10)
    val tickets: MutableList<Ticket> = mutableListOf()

    @OneToMany(
        mappedBy = "user",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @BatchSize(size = 10)
    val logs: MutableList<Log> = mutableListOf()
}