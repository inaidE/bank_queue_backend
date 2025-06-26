package com.bankqueue.bankqueuebackend.controller

import com.bankqueue.bankqueuebackend.dto.TicketCreateDto
import com.bankqueue.bankqueuebackend.dto.TicketResponseDto
import com.bankqueue.bankqueuebackend.service.TicketService
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
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.time.OffsetDateTime

@WebMvcTest(
    controllers = [TicketController::class],
    excludeAutoConfiguration = [
        SecurityAutoConfiguration::class,
        UserDetailsServiceAutoConfiguration::class
    ]
)
@AutoConfigureMockMvc(addFilters = false)
@Import(TicketControllerTest.UserDetailsResolverConfig::class)
class TicketControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var mapper: ObjectMapper

    @MockBean
    private lateinit var ticketService: TicketService

    /**
     * Сценарий: авторизованный пользователь запрашивает список своих тикетов
     * и получает JSON-массив TicketResponseDto с HTTP 200
     */
    @Test
    @WithMockUser(username = "user1", roles = ["USER"])
    fun `GET my tickets returns list`() {
        val now = OffsetDateTime.parse("2025-06-27T10:15:30+00:00")
        val dto = TicketResponseDto(
            id          = 1L,
            userId      = 42L,
            address     = "123 Main St",
            ticketType  = "STANDARD",
            ticket      = "T1",
            scheduledAt = now
        )
        given(ticketService.getAllForUserLogin("user1")).willReturn(listOf(dto))

        mockMvc.perform(get("/api/tickets"))
            .andExpect(status().isOk)
            .andExpect(content().json(mapper.writeValueAsString(listOf(dto))))
    }

    /**
     * Сценарий: авторизованный пользователь создаёт новый тикет через POST /api/tickets,
     * сервис возвращает DTO созданного тикета, контроллер ставит Location и отдаёт HTTP 201
     */
    @Test
    @WithMockUser(username = "user1", roles = ["USER"])
    fun `POST create ticket returns 201 and Location header`() {
        val scheduled = OffsetDateTime.parse("2025-06-27T12:00:00+00:00")
        val createDto = TicketCreateDto(
            address     = "123 Main St",
            ticketType  = "STANDARD",
            scheduledAt = scheduled
        )
        val respDto = TicketResponseDto(
            id          = 5L,
            userId      = 42L,
            address     = "123 Main St",
            ticketType  = "STANDARD",
            ticket      = "T5",
            scheduledAt = scheduled
        )
        given(ticketService.createForUser("user1", createDto)).willReturn(respDto)

        mockMvc.perform(
            post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createDto))
        )
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", "/api/tickets/5"))
            .andExpect(content().json(mapper.writeValueAsString(respDto)))
    }

    /**
     * Регистрируем кастомный резолвер для параметров типа UserDetails,
     * чтобы вместо ModelAttribute Spring подставлял текущего аутентифицированного пользователя.
     */
    @TestConfiguration
    class UserDetailsResolverConfig : WebMvcConfigurer {
        override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
            resolvers.add(UserDetailsArgumentResolver())
        }
    }

    class UserDetailsArgumentResolver : HandlerMethodArgumentResolver {
        override fun supportsParameter(parameter: MethodParameter): Boolean =
            UserDetails::class.java.isAssignableFrom(parameter.parameterType)

        override fun resolveArgument(
            parameter: MethodParameter,
            mavContainer: ModelAndViewContainer?,
            webRequest: NativeWebRequest,
            binderFactory: WebDataBinderFactory?
        ): Any? {
            // Берём principal из SecurityContext, его устанавливает @WithMockUser
            return SecurityContextHolder.getContext().authentication?.principal
        }
    }
}
