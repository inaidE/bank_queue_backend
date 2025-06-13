package com.bankqueue.bankqueuebackend.dto

import com.bankqueue.bankqueuebackend.model.User

/** Entity → ResponseDto */
fun User.toResponseDto() = UserResponseDto(
    id = this.id!!,
    name = this.name,
    login = this.login,
    email = this.email,
    phoneNumber = this.phoneNumber
)

