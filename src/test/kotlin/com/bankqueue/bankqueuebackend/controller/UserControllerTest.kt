package com.bankqueue.bankqueuebackend.controller

import com.bankqueue.bankqueuebackend.dto.UserCreateDto
import com.bankqueue.bankqueuebackend.dto.UserResponseDto
import com.bankqueue.bankqueuebackend.service.UserService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.core.MethodParameter

@WebMvcTest(
    controllers = [UserController::class],
    excludeAutoConfiguration = [
        SecurityAutoConfiguration::class,
        UserDetailsServiceAutoConfiguration::class
    ]
)
@AutoConfigureMockMvc(addFilters = false)
@Import(UserControllerTest.UserDetailsResolverConfig::class)
class UserControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var mapper: ObjectMapper

    @MockBean
    private lateinit var userService: UserService

    /**
     * Сценарий: новый пользователь регистрируется через POST /api/users/register,
     * сервис возвращает данные созданного пользователя,
     * контроллер устанавливает заголовок Location = "/api/users/me" и возвращает HTTP 201.
     */
    @Test
    fun `POST register returns 201 with user and Location header`() {
        val createDto = UserCreateDto(
            name        = "John Doe",
            login       = "johndoe",
            email       = "john@example.com",
            password    = "secret1",
            phoneNumber = "+12345678901"
        )
        val respDto = UserResponseDto(
            id          = 2L,
            name        = "John Doe",
            login       = "johndoe",
            email       = "john@example.com",
            phoneNumber = "+12345678901"
        )
        given(userService.register(createDto)).willReturn(respDto)

        mockMvc.perform(
            post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createDto))
        )
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", "/api/users/me"))
            .andExpect(content().json(mapper.writeValueAsString(respDto)))
    }

    /**
     * Сценарий: авторизованный пользователь запрашивает свой профиль через GET /api/users/me,
     * сервис возвращает UserResponseDto с данными пользователя, контроллер отдаёт HTTP 200.
     */
    @Test
    @WithMockUser(username = "user1", roles = ["USER"])
    fun `GET me returns current user's profile`() {
        val respDto = UserResponseDto(
            id          = 1L,
            name        = "Test User",
            login       = "user1",
            email       = "u1@example.com",
            phoneNumber = "+11111111111"
        )
        given(userService.getForLogin("user1")).willReturn(respDto)

        mockMvc.perform(get("/api/users/me"))
            .andExpect(status().isOk)
            .andExpect(content().json(mapper.writeValueAsString(respDto)))
    }

    /**
     * Конфигурация: добавляем кастомный резолвер аргументов для UserDetails,
     * чтобы @AuthenticationPrincipal работал в тестах и подставлял principal из SecurityContextHolder.
     */
    @TestConfiguration
    class UserDetailsResolverConfig : WebMvcConfigurer {
        override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
            resolvers.add(object : HandlerMethodArgumentResolver {
                override fun supportsParameter(parameter: MethodParameter): Boolean =
                    UserDetails::class.java.isAssignableFrom(parameter.parameterType)

                override fun resolveArgument(
                    parameter: MethodParameter,
                    mavContainer: ModelAndViewContainer?,
                    webRequest: NativeWebRequest,
                    binderFactory: WebDataBinderFactory?
                ): Any? {
                    return SecurityContextHolder.getContext().authentication.principal
                }
            })
        }
    }
}