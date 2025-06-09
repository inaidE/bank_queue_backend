package com.bankqueue.bankqueuebackend.repository

import com.bankqueue.bankqueuebackend.model.Log
import com.bankqueue.bankqueuebackend.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LogRepository : JpaRepository<Log, Long> {
    fun findAllByUserId(userId: Long): List<Log>
    fun findAllByUser(user: User): List<Log>
}
