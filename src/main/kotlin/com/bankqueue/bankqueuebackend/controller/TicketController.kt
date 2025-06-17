package com.bankqueue.bankqueuebackend.controller

import com.bankqueue.bankqueuebackend.dto.TicketCreateDto
import com.bankqueue.bankqueuebackend.dto.TicketResponseDto
import com.bankqueue.bankqueuebackend.dto.TicketUpdateDto
import com.bankqueue.bankqueuebackend.service.TicketService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/api/tickets")
class TicketController(
    private val ticketService: TicketService
) {

    /**
     * Получить все тикеты текущего пользователя
     */
    @GetMapping
    fun getMyTickets(
        @AuthenticationPrincipal principal: UserDetails
    ): ResponseEntity<List<TicketResponseDto>> {
        val tickets = ticketService.getAllForUserLogin(principal.username)
        return ResponseEntity.ok(tickets)
    }

    @GetMapping("/{id}")
    fun getTicketById(
        @AuthenticationPrincipal principal: UserDetails,
        @PathVariable id: Long
    ): ResponseEntity<TicketResponseDto> {
        val dto = ticketService.getByIdForUser(principal.username, id)
        return ResponseEntity.ok(dto)
    }

    /**
     * Создать новый тикет за текущего пользователя
     */
    @PostMapping()
    fun createTicket(
        @AuthenticationPrincipal principal: UserDetails,
        @Valid @RequestBody dto: TicketCreateDto
    ): ResponseEntity<TicketResponseDto> {
        val created = ticketService.createForUser(principal.username, dto)
        val location = URI.create("/api/tickets/${created.id}")
        return ResponseEntity.created(location).body(created)
    }

    /**
     * Частичное обновление тикета текущего пользователя
     */
    @PatchMapping("/{id}")
    fun updateTicket(
        @AuthenticationPrincipal principal: UserDetails,
        @PathVariable id: Long,
        @Valid @RequestBody dto: TicketUpdateDto
    ): ResponseEntity<TicketResponseDto> {
        val updated = ticketService.updateForUser(principal.username, id, dto)
        return ResponseEntity.ok(updated)
    }

    /**
     * Удалить тикет текущего пользователя
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteTicket(
        @AuthenticationPrincipal principal: UserDetails,
        @PathVariable id: Long
    ) {
        ticketService.deleteForUser(principal.username, id)
    }
}