// src/test/kotlin/com/bankqueue/bankqueuebackend/controller/AuthControllerTest.kt
package com.bankqueue.bankqueuebackend.controller

import com.bankqueue.bankqueuebackend.security.JwtTokenProvider
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

@WebMvcTest(
    controllers = [AuthController::class],
    excludeAutoConfiguration = [
        SecurityAutoConfiguration::class,
        UserDetailsServiceAutoConfiguration::class
    ]
)
class AuthControllerTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @MockBean
    private lateinit var authenticationManager: AuthenticationManager

    @MockBean
    private lateinit var jwtTokenProvider: JwtTokenProvider

    private val mapper = jacksonObjectMapper()

    /**
     * Сценарий: успешный POST запрос к /auth/login с корректными учётными данными
     * должен пройти аутентификацию и вернуть JSON с полем "token"
     */
    @Test
    @DisplayName("POST /auth/login — успешно логинит и возвращает токен")
    fun `should login and return token`() {
        // DTO LoginRequest находится в том же пакете, что и контроллер
        val req = LoginRequest(username = "user1", password = "pass")

        // Мокаем успешную аутентификацию в AuthenticationManager
        val fakeAuth = UsernamePasswordAuthenticationToken(
            "user1",
            null,
            emptyList<GrantedAuthority>()
        )
        `when`(
            authenticationManager.authenticate(any<UsernamePasswordAuthenticationToken>())
        ).thenReturn(fakeAuth)

        // Мокаем выдачу JWT-токена
        `when`(
            jwtTokenProvider.createToken("user1")
        ).thenReturn("jwt-token-xyz")

        // Выполняем POST и проверяем ответ
        mvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.token").value("jwt-token-xyz"))
    }
}
