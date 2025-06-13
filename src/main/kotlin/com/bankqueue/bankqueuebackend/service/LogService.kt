package com.bankqueue.bankqueuebackend.service

import com.bankqueue.bankqueuebackend.dto.LogCreateDto
import com.bankqueue.bankqueuebackend.model.Log
import com.bankqueue.bankqueuebackend.repository.LogRepository
import com.bankqueue.bankqueuebackend.repository.UserRepository
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service
import jakarta.persistence.EntityNotFoundException
import java.time.OffsetDateTime

@Service
class LogService(
    private val userRepository: UserRepository,
    private val logRepository: LogRepository)
{
    @Transactional
    fun createLog(userLogin: String, dto: LogCreateDto) {
        val user = userRepository.findByLogin(userLogin)
            ?: throw EntityNotFoundException("User '$userLogin' not found")
        val log = Log(
            user = user,
            eventType = dto.eventType,
            eventTime = OffsetDateTime.now(),
            details = dto.details
        )
        logRepository.save(log)
    }
}