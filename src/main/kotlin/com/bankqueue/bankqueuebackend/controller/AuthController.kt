package com.bankqueue.bankqueuebackend.controller

import com.bankqueue.bankqueuebackend.security.JwtTokenProvider
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.bind.annotation.*

data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val token: String)

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenProvider: JwtTokenProvider
) {
    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): ResponseEntity<LoginResponse> {
        val authToken = UsernamePasswordAuthenticationToken(req.username, req.password)
        val auth = authenticationManager.authenticate(authToken)
        val token = jwtTokenProvider.createToken(auth.name)
        return ResponseEntity.ok(LoginResponse(token))
    }
}