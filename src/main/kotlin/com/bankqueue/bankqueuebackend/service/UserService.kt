package com.bankqueue.bankqueuebackend.service

import com.bankqueue.bankqueuebackend.dto.*
import com.bankqueue.bankqueuebackend.model.User
import com.bankqueue.bankqueuebackend.repository.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val logService: LogService
) {

    /** Получить профиль текущего пользователя по логину */
    @Transactional(readOnly = true)
    fun getForLogin(userLogin: String): UserResponseDto {
        val user = userRepository.findByLogin(userLogin)
            ?: throw EntityNotFoundException("User '$userLogin' not found")
        return user.toResponseDto()
    }

    /** Получить всех пользователей (для админа) (пока не реализовано)*/
    @Transactional(readOnly = true)
    fun getAll(): List<UserResponseDto> =
        userRepository.findAll().map { it.toResponseDto() }

    /** Зарегистрировать нового пользователя (open registration) */
    @Transactional
    fun register(dto: UserCreateDto): UserResponseDto {
        // 0. Проверяем, что логин и email ещё не заняты
        if (userRepository.findByLogin(dto.login) != null) {
            throw IllegalArgumentException("Login '${dto.login}' is already taken")
        }
        if (userRepository.findByEmail(dto.email) != null) {
            throw IllegalArgumentException("Email '${dto.email}' is already registered")
        }

        // 1. Хешируем пароль
        val hashed = passwordEncoder.encode(dto.password)

        // 2. Создаём entity «вручную» (чтобы было всё видно)
        val user = User(
            name              = dto.name,
            login             = dto.login,
            email             = dto.email,
            encryptedPassword = hashed,
            phoneNumber       = dto.phoneNumber
        )

        // 3. Сохраняем в БД — JPA сгенерирует id
        val saved = userRepository.save(user)

        // Логирование
        logService.createLog(
            userLogin = dto.login,
            dto = LogCreateDto(
                eventType = "USER_REGISTERED",
                details   = mapOf("userId" to saved.id!!)
            )
        )

        // 4. Конвертируем сохранённую entity в DTO ответа
        return saved.toResponseDto()
    }

    /** Частично обновить профиль текущего пользователя */
    @Transactional
    fun updateForLogin(userLogin: String, dto: UserUpdateDto): UserResponseDto {
        val user = userRepository.findByLogin(userLogin)
            ?: throw EntityNotFoundException("User '$userLogin' not found")

        // применяем только непустые поля
        dto.name       ?.let { user.name        = it }
        dto.email      ?.let { user.email       = it }
        dto.phoneNumber?.let { user.phoneNumber = it }

        val updated = userRepository.save(user)

        // Логирование
        logService.createLog(
            userLogin = user.login,
            dto = LogCreateDto(
                eventType = "USER_UPDATED",
                details   = mapOf("userId" to updated.id!!)
            )
        )

        return updated.toResponseDto()
    }

    /** Удалить свой аккаунт */
    @Transactional
    fun deleteForLogin(userLogin: String) {
        val user = userRepository.findByLogin(userLogin)
            ?: throw EntityNotFoundException("User '$userLogin' not found")

        userRepository.delete(user)
        // Логирование
        logService.createLog(
            userLogin = user.login,
            dto = LogCreateDto(
                eventType = "USER_DELETED",
                details   = mapOf("userId" to user.id!!)
            )
        )
    }

    /**
     * Сменить пароль текущего пользователя
     */
    @Transactional
    fun changePassword(userLogin: String, dto: ChangePasswordDto) {
        if (dto.newPassword != dto.confirmPassword) {
            throw IllegalArgumentException("Новый пароль и подтверждение не совпадают")
        }
        val user = userRepository.findByLogin(userLogin)
            ?: throw EntityNotFoundException("User '$userLogin' not found")

        if (!passwordEncoder.matches(dto.currentPassword, user.encryptedPassword)) {
            throw IllegalArgumentException("Старый пароль неверен")
        }
        user.encryptedPassword = passwordEncoder.encode(dto.newPassword)
        val saved = userRepository.save(user)

        // Логирование
        logService.createLog(
            userLogin = user.login,
            dto = LogCreateDto(
                eventType = "USER_PASSWORD_UPDATED",
                details   = mapOf("userId" to user.id!!)
            )
        )
    }
}