package com.bankqueue.bankqueuebackend.config

import com.bankqueue.bankqueuebackend.security.JwtTokenFilter
import com.bankqueue.bankqueuebackend.security.JwtTokenProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userDetailsService: UserDetailsService
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(authConfig: AuthenticationConfiguration): AuthenticationManager =
        authConfig.authenticationManager

    @Bean
    @Throws(Exception::class)
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // 1) Отключаем HTTP Basic и форму логина
            .httpBasic().disable()
            .formLogin().disable()
            // 2) Отключаем CSRF — мы работаем stateless
            .csrf().disable()
            // 3) Задаём stateless-сессию (JWT без сессий)
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            // 4) Публичные маршруты + Swagger UI
            .authorizeHttpRequests()
            .requestMatchers("/auth/login").permitAll()
            .requestMatchers("/api/users/register").permitAll()
            .requestMatchers(
                "/swagger-ui.html",
                "/swagger-ui/index.html",
                "/swagger-ui/**",
                "/v3/api-docs/**"
            ).permitAll()
            .anyRequest().authenticated()
            .and()
            // 5) Наш JWT-фильтр
            .addFilterBefore(
                JwtTokenFilter(jwtTokenProvider, userDetailsService),
                UsernamePasswordAuthenticationFilter::class.java
            )

        return http.build()
    }
}