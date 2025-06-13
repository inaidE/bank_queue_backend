package com.bankqueue.bankqueuebackend.dto

data class UserResponseDto(
    val id: Long,
    val name: String,
    val login: String,
    val email: String,
    val phoneNumber: String
)