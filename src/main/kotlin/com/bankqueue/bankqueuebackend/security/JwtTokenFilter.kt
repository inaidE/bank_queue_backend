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
        val path = request.servletPath

        // Здесь — единый список всех публичных URL-ов
        val publicPaths = listOf(
            "/auth/login",
            "/api/users/register",
            "/swagger-ui.html",
            "/swagger-ui/index.html",
            "/swagger-ui/",       // для ресурсов
            "/v3/api-docs",       // OpenAPI JSON
            "/v3/api-docs/"       // и для подпутей
        )

        // Если путь начинается с любого публичного — пропускаем
        if (publicPaths.any { path.startsWith(it) }) {
            filterChain.doFilter(request, response)
            return
        }

        // иначе — стандартная JWT-валидация
        val header = request.getHeader("Authorization")
        if (header == null || !header.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Missing or invalid Authorization header")
            return
        }

        val token = header.substringAfter("Bearer ")
        if (!jwtTokenProvider.validateToken(token)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or expired JWT token")
            return
        }

        val username = jwtTokenProvider.getUsername(token)
        val userDetails = userDetailsService.loadUserByUsername(username)
        val auth = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
        SecurityContextHolder.getContext().authentication = auth

        // и только после всего — пропускаем дальше
        filterChain.doFilter(request, response)
    }
}
