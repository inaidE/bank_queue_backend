package com.bankqueue.bankqueuebackend.service

import com.bankqueue.bankqueuebackend.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByLogin(username)
            ?: throw UsernameNotFoundException("User '$username' not found")
        // Преобразуем вашу сущность User в Spring Security UserDetails.
        // Если у вас нет ролей, можно просто:
        return org.springframework.security.core.userdetails.User(
            user.login,
            user.encryptedPassword,     // поле с уже захэшированным паролем
            emptyList()            // список GrantedAuthority, пустой, если у вас нет ролей
        )
    }
}