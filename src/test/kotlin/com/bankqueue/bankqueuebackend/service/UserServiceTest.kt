package com.bankqueue.bankqueuebackend.service

import com.bankqueue.bankqueuebackend.dto.ChangePasswordDto
import com.bankqueue.bankqueuebackend.dto.LogCreateDto
import com.bankqueue.bankqueuebackend.dto.UserCreateDto
import com.bankqueue.bankqueuebackend.dto.UserUpdateDto
import com.bankqueue.bankqueuebackend.model.User
import com.bankqueue.bankqueuebackend.repository.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.lenient
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.crypto.password.PasswordEncoder

@ExtendWith(MockitoExtension::class)
class UserServiceTest {

    @Mock private lateinit var userRepository: UserRepository
    @Mock private lateinit var passwordEncoder: PasswordEncoder
    @Mock private lateinit var logService: LogService
    @InjectMocks private lateinit var userService: UserService

    /**
     * Сценарий: при запросе существующего пользователя по логину
     * возвращаются корректные данные в UserResponseDto
     */
    @Test
    fun find_user_by_login() {
        // GIVEN
        val user = User(
            id = 1L,
            name = "Владимир",
            login = "vladimir",
            email = "vladimir@gmail.com",
            encryptedPassword = "hash",
            phoneNumber = "+79000000000"
        )
        whenever(userRepository.findByLogin("vladimir")).thenReturn(user)

        // WHEN
        val dto = userService.getForLogin("vladimir")

        // THEN
        assertEquals(user.id, dto.id)
        assertEquals(user.name, dto.name)
        assertEquals(user.login, dto.login)
        assertEquals(user.email, dto.email)
        assertEquals(user.phoneNumber, dto.phoneNumber)
    }

    /**
     * Сценарий: при запросе пользователя по email из репозитория
     * возвращается правильная сущность User
     */
    @Test
    fun find_user_byEmail() {
        // GIVEN
        val user = User(
            id = 1L,
            name = "Владимир",
            login = "vladimir",
            email = "vladimir@gmail.com",
            encryptedPassword = "hash",
            phoneNumber = "+79000000000"
        )
        whenever(userRepository.findByEmail("vladimir@gmail.com")).thenReturn(user)

        // WHEN
        val result = userRepository.findByEmail("vladimir@gmail.com")

        // THEN
        assertEquals(user.id, result?.id)
        assertEquals(user.name, result?.name)
        assertEquals(user.login, result?.login)
        assertEquals(user.email, result?.email)
        assertEquals(user.phoneNumber, result?.phoneNumber)
    }

    /**
     * Сценарий: при запросе несуществующего пользователя по логину
     * выбрасывается EntityNotFoundException
     */
    @Test
    fun find_user_by_login_not_found_throws() {
        // GIVEN
        whenever(userRepository.findByLogin("stanislav29")).thenReturn(null)

        // WHEN / THEN
        assertThrows<EntityNotFoundException> {
            userService.getForLogin("stanislav29")
        }
    }

    /**
     * Сценарий: регистрация с дублирующимся логином
     * вызывает IllegalArgumentException
     */
    @Test
    fun register_duplicate_login_throws() {
        // GIVEN
        val req = UserCreateDto(
            name = "Пётр",
            login = "petr",
            email = "petr@example.com",
            password = "pass",
            phoneNumber = "+70001112233"
        )
        whenever(userRepository.findByLogin("petr"))
            .thenReturn(User(2L, "Пётр", "petr", "x@e", "h", "+7"))

        // WHEN / THEN
        assertThrows<IllegalArgumentException> {
            userService.register(req)
        }
    }

    /**
     * Сценарий: регистрация с дублирующимся email
     * вызывает IllegalArgumentException
     */
    @Test
    fun register_duplicate_email_throws() {
        // GIVEN
        val req = UserCreateDto("Ирина", "irina", "irina@e.com", "pw", "+70009998877")
        whenever(userRepository.findByLogin("irina")).thenReturn(null)
        whenever(userRepository.findByEmail("irina@e.com"))
            .thenReturn(User(3L, "Ирина", "x", "irina@e.com", "h", "+7"))

        // WHEN / THEN
        assertThrows<IllegalArgumentException> {
            userService.register(req)
        }
    }

    /**
     * Сценарий: успешная регистрация нового пользователя,
     * проверка сохранённых данных и логирования события
     */
    @Test
    fun register_new_user_succeeds_and_logs() {
        // GIVEN
        val req = UserCreateDto("Анна", "anna", "anna@e.com", "pwd", "+79001234567")
        whenever(userRepository.findByLogin("anna")).thenReturn(null)
        whenever(userRepository.findByEmail("anna@e.com")).thenReturn(null)
        whenever(passwordEncoder.encode("pwd")).thenReturn("ENC")

        val saved = User(10L, "Анна", "anna", "anna@e.com", "ENC", "+79001234567")

        doAnswer { invocation ->
            val toSave = invocation.getArgument<User>(0)
            toSave.id = 10L
            toSave
        }.whenever(userRepository).save(any())

        // WHEN
        val dto = userService.register(req)

        // THEN
        assertEquals(10L, dto.id)
        assertEquals("Анна", dto.name)

        // AND: событие залогировано
        val cap = argumentCaptor<LogCreateDto>()
        verify(logService).createLog(eq("anna"), cap.capture())
        assertEquals("USER_REGISTERED", cap.firstValue.eventType)
        assertEquals(mapOf("userId" to 10L), cap.firstValue.details)
    }

    /**
     * Сценарий: успешное обновление имени и email существующего пользователя
     * с последующим логированием
     */
    @Test
    fun update_existing_user_succeeds_and_logs() {
        // GIVEN
        val existing = User(5L, "OldName", "user5", "u5@e", "h", "+7000")
        whenever(userRepository.findByLogin("user5")).thenReturn(existing)

        val req = UserUpdateDto(name = "NewName", email = "new@e", phoneNumber = "+7111")

        // Вместо simple thenReturn, эмулируем сохранение и возвращаем обновлённый объект:
        doAnswer { invocation ->
            val toSave = invocation.getArgument<User>(0)
            // сохраняем изменения
            toSave.name = req.name!!
            toSave.email = req.email!!
            toSave.phoneNumber = req.phoneNumber!!
            toSave
        }.whenever(userRepository).save(any())

        // WHEN
        val dto = userService.updateForLogin("user5", req)

        // THEN
        assertEquals("NewName", dto.name)
        assertEquals("new@e", dto.email)
        assertEquals("+7111", dto.phoneNumber)

        // AND: событие залогировано
        val cap = argumentCaptor<LogCreateDto>()
        verify(logService).createLog(eq("user5"), cap.capture())
        assertEquals("USER_UPDATED", cap.firstValue.eventType)
    }


    /**
     * Сценарий: успешное удаление существующего пользователя
     * с последующим логированием удаления
     */
    @Test
    fun delete_existing_user_succeeds_and_logs() {
        // GIVEN
        val user = User(7L, "Zoya", "zoya", "z@e", "h", "+7999")
        whenever(userRepository.findByLogin("zoya")).thenReturn(user)

        // WHEN
        userService.deleteForLogin("zoya")

        // THEN
        verify(userRepository).delete(user)
        val cap = argumentCaptor<LogCreateDto>()
        verify(logService).createLog(eq("zoya"), cap.capture())
        assertEquals("USER_DELETED", cap.firstValue.eventType)
    }

    /**
     * Сценарий: попытка удаления несуществующего пользователя
     * вызывает EntityNotFoundException
     */
    @Test
    fun delete_user_not_found_throws() {
        whenever(userRepository.findByLogin("noone")).thenReturn(null)

        assertThrows<EntityNotFoundException> {
            userService.deleteForLogin("noone")
        }
    }

    /**
     * Сценарий: попытка смены пароля, когда новый пароль и подтверждение не совпадают,
     * вызывает IllegalArgumentException
     */
    @Test
    fun change_password_mismatch_confirmation_throws() {
        // GIVEN: делаем «ленивый» стаб, чтобы Mockito не считал его лишним
        val dummyUser = User(
            id = 100L,
            name = "Dummy",
            login = "any",
            email = "any@e",
            encryptedPassword = "hash",
            phoneNumber = "+7000"
        )
        lenient()
            .`when`(userRepository.findByLogin("any"))
            .thenReturn(dummyUser)

        // WHEN / THEN
        val dto = ChangePasswordDto("old", "new1", "new2")
        assertThrows<IllegalArgumentException> {
            userService.changePassword("any", dto)
        }
    }

    /**
     * Сценарий: попытка смены пароля с неверным текущим паролем,
     * вызывает IllegalArgumentException
     */
    @Test
    fun change_password_wrong_current_throws() {
        // GIVEN
        val user = User(8L, "U8", "u8", "u8@e", "HASH", "+7000")
        whenever(userRepository.findByLogin("u8")).thenReturn(user)
        whenever(passwordEncoder.matches("wrong", "HASH")).thenReturn(false)
        val dto = ChangePasswordDto("wrong", "new", "new")

        // WHEN / THEN
        assertThrows<IllegalArgumentException> {
            userService.changePassword("u8", dto)
        }
    }

    /**
     * Сценарий: успешная смена пароля при корректном текущем пароле,
     * проверка сохранения нового хэша и логирования события
     */
    @Test
    fun change_password_success_and_logs() {
        val user = User(9L, "U9", "u9", "u9@e", "HASH1", "+7000")
        whenever(userRepository.findByLogin("u9")).thenReturn(user)
        whenever(passwordEncoder.matches("old", "HASH1")).thenReturn(true)
        whenever(passwordEncoder.encode("new")).thenReturn("HASH2")

        doAnswer { invocation ->
            val toSave = invocation.getArgument<User>(0)
            toSave.encryptedPassword = "HASH2"
            toSave
        }.whenever(userRepository).save(any())

        userService.changePassword("u9", ChangePasswordDto("old",  "new", "new"))

        val cap = argumentCaptor<LogCreateDto>()
        verify(logService).createLog(eq("u9"), cap.capture())
        assertEquals("USER_PASSWORD_UPDATED", cap.firstValue.eventType)
    }

}
