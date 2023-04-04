package xyz.le30r.bot.users.token

import java.time.Instant

data class Token(
    val data: String,
    val expiresAt: Instant,
    val createdBy: String
)
