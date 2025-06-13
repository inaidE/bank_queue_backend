package com.bankqueue.bankqueuebackend.controller

import com.bankqueue.bankqueuebackend.dto.*
import com.bankqueue.bankqueuebackend.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {

    /**
     * Зарегистрировать нового пользователя
     */
    @PostMapping("/register")
    fun register(
        @Valid @RequestBody dto: UserCreateDto
    ): ResponseEntity<UserResponseDto> {
        val created = userService.register(dto)
        // вернуть 201 Created и Location
        val location = URI.create("/api/users/me")
        return ResponseEntity.created(location).body(created)
    }

    /**
     * Получить профиль текущего пользователя
     */
    @GetMapping("/me")
    fun getMyProfile(
        @AuthenticationPrincipal principal: UserDetails
    ): ResponseEntity<UserResponseDto> {
        val profile = userService.getForLogin(principal.username)
        return ResponseEntity.ok(profile)
    }

    /**
     * Частичное обновление профиля текущего пользователя
     */
    @PatchMapping("/me")
    fun updateMyProfile(
        @AuthenticationPrincipal principal: UserDetails,
        @Valid @RequestBody dto: UserUpdateDto
    ): ResponseEntity<UserResponseDto> {
        val updated = userService.updateForLogin(principal.username, dto)
        return ResponseEntity.ok(updated)
    }

    /**
     * Сменить пароль текущего пользователя
     */
    @PostMapping("/me/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun changePassword(
        @AuthenticationPrincipal principal: UserDetails,
        @Valid @RequestBody dto: ChangePasswordDto
    ) {
        userService.changePassword(principal.username, dto)
    }

    /**
     * Удалить аккаунт текущего пользователя
     */
    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteMyAccount(
        @AuthenticationPrincipal principal: UserDetails
    ) {
        userService.deleteForLogin(principal.username)
    }

    /**
     * (Опционально) Получить всех пользователей — например, для роли ADMIN (не реализовано)
     */
    @GetMapping
    fun getAllUsers(): ResponseEntity<List<UserResponseDto>> {
        val list = userService.getAll()
        return ResponseEntity.ok(list)
    }
}