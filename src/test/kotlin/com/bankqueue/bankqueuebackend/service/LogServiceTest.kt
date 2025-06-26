package com.bankqueue.bankqueuebackend.service

import com.bankqueue.bankqueuebackend.dto.LogCreateDto
import com.bankqueue.bankqueuebackend.model.Log
import com.bankqueue.bankqueuebackend.model.User
import com.bankqueue.bankqueuebackend.repository.LogRepository
import com.bankqueue.bankqueuebackend.repository.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class LogServiceTest {

    @Mock private lateinit var userRepository: UserRepository
    @Mock private lateinit var logRepository: LogRepository
    @InjectMocks private lateinit var logService: LogService

    /**
     * Сценарий: попытка создать лог для несуществующего пользователя
     * должна привести к EntityNotFoundException
     */
    @Test
    fun createLog_withUnknownUser_throwsEntityNotFoundException() {
        // GIVEN: в репозитории нет пользователя с логином "ghost"
        whenever(userRepository.findByLogin("ghost")).thenReturn(null)
        val dto = LogCreateDto(eventType = "EV", details = mapOf("x" to 1))

        // WHEN / THEN: ожидаем EntityNotFoundException
        assertThrows<EntityNotFoundException> {
            logService.createLog("ghost", dto)
        }
    }

    /**
     * Сценарий: создание лога для существующего пользователя
     * должно сохранить запись в репозитории с корректными полями
     */
    @Test
    fun createLog_withValidUser_savesLogWithCorrectFields() {
        // GIVEN: существующий пользователь в репозитории
        val user = User(
            id = 7L,
            name = "Test",
            login = "tester",
            email = "t@test",
            encryptedPassword = "h",
            phoneNumber = "+7000"
        )
        whenever(userRepository.findByLogin("tester")).thenReturn(user)
        val dto = LogCreateDto(eventType = "USER_LOGIN", details = mapOf("ip" to "127.0.0.1"))

        // WHEN: вызываем сервис для создания лога
        logService.createLog("tester", dto)

        // THEN: logRepository.save() вызван с сущностью Log, содержащей правильного пользователя, eventType и details
        val captor = argumentCaptor<Log>()
        verify(logRepository).save(captor.capture())
        val savedLog = captor.firstValue

        assertEquals(user, savedLog.user)
        assertEquals("USER_LOGIN", savedLog.eventType)
        assertEquals(mapOf("ip" to "127.0.0.1"), savedLog.details)
    }
}
