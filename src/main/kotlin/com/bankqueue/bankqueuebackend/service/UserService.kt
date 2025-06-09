package com.bankqueue.bankqueuebackend.service

import com.bankqueue.bankqueuebackend.model.User
import com.bankqueue.bankqueuebackend.repository.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service

@Service
class UserService(
    private val repository: UserRepository
    )
{

}