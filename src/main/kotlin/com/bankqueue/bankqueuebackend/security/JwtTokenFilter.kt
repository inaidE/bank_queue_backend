package com.bankqueue.bankqueuebackend.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.web.filter.OncePerRequestFilter

class JwtTokenFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userDetailsService: UserDetailsService
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(JwtTokenFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // Логируем заголовок для отладки
        val header = request.getHeader("Authorization")
        logger.info(">>> Authorization header: $header")

        header
            ?.takeIf { it.startsWith("Bearer ") }
            ?.substringAfter("Bearer ")
            ?.let { token ->
                // Проверяем токен
                val valid = jwtTokenProvider.validateToken(token)
                logger.info(">>> Token valid? $valid")
                if (valid) {
                    // Извлекаем username и загружаем UserDetails
                    val username = jwtTokenProvider.getUsername(token)
                    logger.info(">>> Token username: $username")
                    val userDetails = userDetailsService.loadUserByUsername(username)

                    // Создаём Authentication и сохраняем в контекст
                    val auth = UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.authorities
                    )
                    SecurityContextHolder.getContext().authentication = auth
                    logger.info(">>> Authentication set in context: $auth")
                }
            }

        // Продолжаем цепочку фильтров
        filterChain.doFilter(request, response)
    }
}
