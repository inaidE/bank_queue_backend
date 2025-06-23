package com.bankqueue.bankqueuebackend.service

import com.bankqueue.bankqueuebackend.dto.*
import com.bankqueue.bankqueuebackend.model.Ticket
import com.bankqueue.bankqueuebackend.repository.TicketRepository
import com.bankqueue.bankqueuebackend.repository.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.temporal.ChronoUnit

@Service
class TicketService(
    private val ticketRepository: TicketRepository,
    private val userRepository: UserRepository,
    private val logService: LogService
) {

    /** Получить все тикеты */
    @Transactional(readOnly = true)
    fun getAll(): List<TicketResponseDto> =
        ticketRepository.findAll()
            .map { it.toResponseDto() }


    /** Получить тикет по id */
    @Transactional(readOnly = true)
    fun getByIdForUser(userLogin: String, ticketId: Long): TicketResponseDto {
        // найдём тикет только если он принадлежит текущему пользователю
        val ticket = ticketRepository.findByIdAndUserLogin(ticketId, userLogin)
            ?: throw AccessDeniedException("Ticket $ticketId not found or not yours")
        return ticket.toResponseDto()
    }



    /** Получить тикеты по логину пользователя */
    @Transactional(readOnly = true)
    fun getAllForUserLogin(userLogin: String): List<TicketResponseDto> {
        val user = userRepository.findByLogin(userLogin)
            ?: throw EntityNotFoundException("User '$userLogin' not found")
        return ticketRepository.findAllByUser(user)
            .map { it.toResponseDto() }
    }

    private val prefixMap = mapOf(
        "Вклад"        to "A",
        "Кредит"       to "B",
        "Карты"        to "C",
        "Инвестиции"   to "D",
        "Счета"        to "E",
    )

    /** Создать новый тикет */
    @Transactional
    fun createForUser(userLogin: String, dto: TicketCreateDto): TicketResponseDto {
        val user = userRepository.findByLogin(userLogin)
            ?: throw EntityNotFoundException("User '$userLogin' not found")

        // Обрезаем до минут
        val atMinute = dto.scheduledAt.truncatedTo(ChronoUnit.MINUTES)

        // Проверяем дубль
        if (ticketRepository.existsByAddressAndScheduledAt(dto.address, atMinute)) {
            throw IllegalStateException("Тикет на адрес '${dto.address}' и время '$atMinute' уже существует")
        }

        // остальная логика номер-генерации...
        val prefix = prefixMap[dto.ticketType]
            ?: throw IllegalArgumentException("Unknown ticketType ${dto.ticketType}")
        val maxCode = ticketRepository.findMaxTicketCodeByType(dto.ticketType)
        val nextNumber = maxCode
            ?.substring(1)
            ?.toIntOrNull()
            ?.plus(1)
            ?: 1
        val ticketCode = "$prefix$nextNumber"


        val entity = Ticket(
            user        = user,
            address     = dto.address,
            ticketType  = dto.ticketType,
            ticket      = ticketCode,
            scheduledAt = atMinute
        )
        val saved = ticketRepository.save(entity)

        logService.createLog(
            userLogin = user.login,
            dto = LogCreateDto(
                eventType = "TICKET_CREATED",
                details   = mapOf("userId" to user.id!!, "ticketId" to saved.id!!)
            )
        )

        return saved.toResponseDto()
    }

    /** Частично обновить тикет */
    @Transactional
    fun updateForUser(userLogin: String, id: Long, dto: TicketUpdateDto): TicketResponseDto {
        val user = userRepository.findByLogin(userLogin)
            ?: throw EntityNotFoundException("User '$userLogin' not found")

        val ticket = ticketRepository.findByIdAndUserLogin(id, userLogin)
            ?: throw AccessDeniedException("Ticket $id not found or not yours")

        dto.scheduledAt?.let { raw ->
            val atMinute = raw.truncatedTo(ChronoUnit.MINUTES)
            // если адрес тоже меняется — используем новый, иначе старый
            val addr = dto.address ?: ticket.address
            if (ticketRepository.existsByAddressAndScheduledAt(addr, atMinute)) {
                throw IllegalStateException("Тикет на этот адрес и время уже существует")
            }
            ticket.scheduledAt = atMinute
        }
        dto.address?.let {
            // если время не менялось, тоже стоило бы повторно проверять дубль
            ticket.address = it
        }

        // Если меняется тип – пересоздаём ticket-код
        dto.ticketType?.let {
            ticket.ticketType = it

            val prefix = prefixMap[it]
                ?: throw IllegalArgumentException("Unknown ticketType $it")

            // Сохраняем текущий порядковый номер, если хотим его оставить, либо считаем заново:
            val maxCode = ticketRepository.findMaxTicketCodeByType(it)
            val nextNum = maxCode?.substring(1)?.toIntOrNull()?.plus(1) ?: 1
            ticket.ticket = "$prefix$nextNum"
        }

        val updated = ticketRepository.save(ticket)

        logService.createLog(
            userLogin = user.login,
            dto = LogCreateDto(
                eventType = "TICKET_UPDATED",
                details   = mapOf("userId" to user.id!!,
                                  "ticketId" to updated.id!!)
            )
        )

        return updated.toResponseDto()
    }

    /** Удаление тикета */
    @Transactional
    fun deleteForUser(userLogin: String, id: Long) {
        val user = userRepository.findByLogin(userLogin)
            ?: throw EntityNotFoundException("User '$userLogin' not found")

        val ticket = ticketRepository.findByIdAndUserLogin(id, userLogin)
            ?: throw AccessDeniedException("Ticket $id not found or not yours")
        ticketRepository.delete(ticket)

        logService.createLog(
            userLogin = user.login,
            dto = LogCreateDto(
                eventType = "TICKET_DELETED",
                details   = mapOf("userId" to user.id!!,
                                  "ticketId" to ticket.id!!)
            )
        )
    }
}