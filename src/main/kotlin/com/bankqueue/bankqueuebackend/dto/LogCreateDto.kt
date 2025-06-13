package com.bankqueue.bankqueuebackend.dto

data class LogCreateDto(
    val eventType: String,
    val details: Map<String, Any>
)