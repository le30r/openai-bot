package xyz.le30r.bot.users.token

import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.ZoneOffset

class TokenService {
    private val repository = TokenRepository()
    private val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    fun generateToken(userId: String): Token {
        val token = Token(generate(), LocalDateTime.now().plusDays(7).toInstant(ZoneOffset.UTC), userId)
        repository.save(token)
        return token
    }

    private fun generate(): String = (1..22).map { allowedChars.random() }.joinToString("")

    fun isValidToken(data: String): Boolean = repository.find(data)?.let {
        if (it.expiresAt > LocalDateTime.now().toInstant(ZoneOffset.UTC)) {
            return@let true
        }
        return@let false
    } ?: false
}
