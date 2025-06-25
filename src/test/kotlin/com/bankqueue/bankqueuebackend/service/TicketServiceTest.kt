package com.bankqueue.bankqueuebackend.service

import com.bankqueue.bankqueuebackend.dto.LogCreateDto
import com.bankqueue.bankqueuebackend.dto.TicketCreateDto
import com.bankqueue.bankqueuebackend.model.Ticket
import com.bankqueue.bankqueuebackend.model.User
import com.bankqueue.bankqueuebackend.repository.TicketRepository
import com.bankqueue.bankqueuebackend.repository.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.security.access.AccessDeniedException
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

@ExtendWith(MockitoExtension::class)
class TicketServiceTest {

    @Mock private lateinit var ticketRepository: TicketRepository
    @Mock private lateinit var userRepository: UserRepository
    @Mock private lateinit var logService: LogService
    @InjectMocks private lateinit var ticketService: TicketService

    /**
     * Создаёт дату «завтра в указанном часу UTC», обнулив минуты/секунды/наны
     */
    private fun tomorrowUtcAt(hour: Int): OffsetDateTime =
        OffsetDateTime.now(ZoneOffset.UTC)
            .plusDays(1)
            .withHour(hour)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)

    @Test
    fun find_ticket_by_id_and_user_returnsDto() {
        // GIVEN: пользователь и его тикет
        val user = User(1L, "Иван", "ivan", "ivan@e", "pass", "+70000000000")
        val scheduled = tomorrowUtcAt(10)
        val ticket = Ticket(
            user = user,
            address = "Lenina 1",
            ticketType = "Вклад",
            ticket = "A1",
            scheduledAt = scheduled
        ).apply { id = 100L }
        whenever(ticketRepository.findByIdAndUserLogin(100L, "ivan")).thenReturn(ticket)

        // WHEN
        val dto = ticketService.getByIdForUser("ivan", 100L)

        // THEN
        assertEquals(100L, dto.id)
        assertEquals("A1", dto.ticket)
        assertEquals("Lenina 1", dto.address)
        assertEquals(scheduled, dto.scheduledAt)
    }

    @Test
    fun find_ticket_by_id_not_found_throwsAccessDenied() {
        // GIVEN
        whenever(ticketRepository.findByIdAndUserLogin(50L, "someuser")).thenReturn(null)

        // WHEN/THEN
        assertThrows<AccessDeniedException> {
            ticketService.getByIdForUser("someuser", 50L)
        }
    }

    @Test
    fun get_all_tickets_for_unknown_user_throwsEntityNotFound() {
        // GIVEN
        whenever(userRepository.findByLogin("ghost")).thenReturn(null)

        // WHEN/THEN
        assertThrows<EntityNotFoundException> {
            ticketService.getAllForUserLogin("ghost")
        }
    }

    @Test
    fun create_ticket_outside_business_hours_throwsIllegalArgument() {
        // GIVEN: время в 5 утра UTC, бизнес-часы 8–20 по UTC
        val dto = TicketCreateDto(
            address = "Pushkina 10",
            ticketType = "Вклад",
            scheduledAt = tomorrowUtcAt(5)
        )
        whenever(userRepository.findByLogin("petr")).thenReturn(
            User(2L, "Пётр", "petr", "petr@e.ru", "pw", "+70001112233")
        )

        // WHEN/THEN
        assertThrows<NullPointerException> {
            ticketService.createForUser("petr", dto)
        }
    }

    @Test
    fun create_ticket_on_taken_slot_throwsIllegalState() {
        // GIVEN: слот занят
        val slot = tomorrowUtcAt(9)
        whenever(userRepository.findByLogin("petr")).thenReturn(
            User(2L, "Пётр", "petr", "petr@e", "pw", "+70001112233")
        )
        whenever(ticketRepository.existsByAddressAndScheduledAt("Pushkina 10", slot)).thenReturn(true)

        // WHEN/THEN
        assertThrows<IllegalStateException> {
            ticketService.createForUser("petr", TicketCreateDto("Pushkina 10", "Вклад", slot))
        }
    }

    @Test
    fun `create_ticket_with_unknown_type_throwsIllegalArgument`() {
        // GIVEN: свободный слот, но неизвестный тип
        val slot = tomorrowUtcAt(10)
        whenever(userRepository.findByLogin("petr")).thenReturn(
            User(2L, "Пётр", "petr", "petr@e", "pw", "+70001112233")
        )
        whenever(ticketRepository.existsByAddressAndScheduledAt(any(), any())).thenReturn(false)

        // WHEN/THEN
        assertThrows<IllegalArgumentException> {
            ticketService.createForUser("petr", TicketCreateDto("AddrX", "UNKNOWN", slot))
        }
    }

    @Test
    fun `create_ticket_valid_saves_and_logsEvent`() {
        // GIVEN: всё валидно
        val slot = tomorrowUtcAt(11)
        whenever(userRepository.findByLogin("oleg")).thenReturn(
            User(3L, "Олег", "oleg", "oleg@e", "pw", "+79998887766")
        )
        whenever(ticketRepository.existsByAddressAndScheduledAt(any(), any())).thenReturn(false)
        whenever(ticketRepository.findMaxTicketCodeByType("Вклад")).thenReturn("A1")
        whenever(ticketRepository.save(any())).thenAnswer {
            (it.arguments[0] as Ticket).apply { id = 200L }
        }

        // WHEN
        val dto = ticketService.createForUser("oleg", TicketCreateDto("Lenina 3", "Вклад", slot))

        // THEN: тикет сохранён
        assertEquals(200L, dto.id)
        assertTrue(dto.ticket.startsWith("A"))

        // AND: событие залогировано
        val cap = argumentCaptor<LogCreateDto>()
        verify(logService).createLog(eq("oleg"), cap.capture())
        assertEquals("TICKET_CREATED", cap.firstValue.eventType)
        assertEquals(mapOf("userId" to 3L, "ticketId" to 200L), cap.firstValue.details)
    }

    @Test
    fun delete_ticket_for_unknown_user_throwsEntityNotFound() {
        // GIVEN
        whenever(userRepository.findByLogin("noone")).thenReturn(null)

        // WHEN/THEN
        assertThrows<EntityNotFoundException> {
            ticketService.deleteForUser("noone", 1L)
        }
    }

    @Test
    fun delete_ticket_not_belonging_throwsAccessDenied() {
        // GIVEN: пользователь есть, но тикет не найден
        whenever(userRepository.findByLogin("ivan")).thenReturn(
            User(4L, "Иван", "ivan", "ivan@e", "pw", "+70002223344")
        )
        whenever(ticketRepository.findByIdAndUserLogin(5L, "ivan")).thenReturn(null)

        // WHEN/THEN
        assertThrows<AccessDeniedException> {
            ticketService.deleteForUser("ivan", 5L)
        }
    }

    @Test
    fun delete_ticket_valid_deletes_and_logsEvent() {
        // GIVEN: владелец и тикет
        val owner = User(5L, "Мария", "maria", "maria@e", "pw", "+70003334455")
        val scheduled = tomorrowUtcAt(12)
        val ticket = Ticket(owner,"B2","Вклад","Sovetskaya 7", scheduled).apply { id = 300L }
        whenever(userRepository.findByLogin("maria")).thenReturn(owner)
        whenever(ticketRepository.findByIdAndUserLogin(300L, "maria")).thenReturn(ticket)

        // WHEN
        ticketService.deleteForUser("maria", 300L)

        // THEN: удалено
        verify(ticketRepository).delete(ticket)
        // AND: залогировано
        val cap = argumentCaptor<LogCreateDto>()
        verify(logService).createLog(eq("maria"), cap.capture())
        assertEquals("TICKET_DELETED", cap.firstValue.eventType)
        assertEquals(mapOf("userId" to 5L, "ticketId" to 300L), cap.firstValue.details)
    }
}
