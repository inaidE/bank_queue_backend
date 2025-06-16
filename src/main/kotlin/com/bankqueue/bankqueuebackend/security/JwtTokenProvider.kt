package com.bankqueue.bankqueuebackend.security

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") private val secretKey: String,
    @Value("\${jwt.expiration-ms}") private val validityInMs: Long
) {
    private val key = Keys.hmacShaKeyFor(secretKey.toByteArray())

    fun createToken(username: String): String {
        val now = Date()
        val exp = Date(now.time + validityInMs)
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(exp)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    fun getUsername(token: String): String =
        Jwts.parserBuilder().setSigningKey(key).build()
            .parseClaimsJws(token).body.subject

    fun validateToken(token: String): Boolean = try {
        Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)
        true
    } catch (_: JwtException) {
        false
    }
}