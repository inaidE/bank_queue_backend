package com.bankqueue.bankqueuebackend.service

import com.bankqueue.bankqueuebackend.dto.TicketCreateDto
import com.bankqueue.bankqueuebackend.dto.TicketResponseDto
import com.bankqueue.bankqueuebackend.dto.TicketUpdateDto
import com.bankqueue.bankqueuebackend.dto.toResponseDto
import com.bankqueue.bankqueuebackend.model.Ticket
import com.bankqueue.bankqueuebackend.repository.TicketRepository
import com.bankqueue.bankqueuebackend.repository.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TicketService(
    private val ticketRepository: TicketRepository,
    private val userRepository: UserRepository
) {

    /** Получить все тикеты */
    @Transactional(readOnly = true)
    fun getAll(): List<TicketResponseDto> =
        ticketRepository.findAll()
            .map { it.toResponseDto() }



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
        // 1. находим пользователя
        val user = userRepository.findByLogin(userLogin)
            ?: throw EntityNotFoundException("User '$userLogin' not found")

        // 2. определяем префикс
        val prefix = prefixMap[dto.ticketType]
            ?: throw IllegalArgumentException("Unknown ticketType ${dto.ticketType}")

        // 3. берём максимальный существующий код вида "В123"
        val maxCode = ticketRepository.findMaxTicketCodeByType(dto.ticketType)
        // 4. парсим номер и увеличиваем
        val nextNumber = maxCode
            ?.substring(1)
            ?.toIntOrNull()
            ?.plus(1)
            ?: 1  // если записей ещё нет — начинаем с 1

        // 5. собираем итоговый код
        val ticketCode = "$prefix$nextNumber"

        // 6. создаём и сохраняем сущность
        val entity = Ticket(
            user        = user,
            address     = dto.address,
            ticketType  = dto.ticketType,
            ticket      = ticketCode,
            scheduledAt = dto.scheduledAt
        )
        val saved = ticketRepository.save(entity)

        // 7. возвращаем DTO
        return saved.toResponseDto()
    }

    /** Частично обновить тикет */
    @Transactional
    fun updateForUser(userLogin: String, id: Long, dto: TicketUpdateDto): TicketResponseDto {
        val ticket = ticketRepository.findByIdAndUserLogin(id, userLogin)
            ?: throw AccessDeniedException("Ticket $id not found or not yours")

        // Если меняется адрес или время – просто присваиваем
        dto.address?.let    { ticket.address     = it }
        dto.scheduledAt?.let{ ticket.scheduledAt = it }

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
        return updated.toResponseDto()
    }

    /** Удаление тикета */
    @Transactional
    fun deleteForUser(userLogin: String, id: Long) {
        val ticket = ticketRepository.findByIdAndUserLogin(id, userLogin)
            ?: throw AccessDeniedException("Ticket $id not found or not yours")
        ticketRepository.delete(ticket)
    }
}