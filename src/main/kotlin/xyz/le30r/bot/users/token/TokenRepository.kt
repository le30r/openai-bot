package xyz.le30r.bot.users.token

import xyz.le30r.bot.Application
import xyz.le30r.bot.users.User
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Timestamp
import java.util.*

class TokenRepository {
    private val connection: Connection = DriverManager.getConnection(Application.Secrets.jdbcUrl)
    fun save(entity: Token) {
        val statement = connection.prepareStatement("INSERT INTO token (data, created_by, expires_at) VALUES (?, ?, ?)")
        statement.setString(1, entity.data)
        statement.setString(2, entity.createdBy)
        statement.setTimestamp(3, Timestamp.from(entity.expiresAt))
        statement.executeUpdate()
    }

    fun find(data: String): Token? {
        val statement = connection.prepareStatement("SELECT data, expires_at, created_by FROM token WHERE data = ?")
        statement.setString(1, data)
        val rs = statement.executeQuery()
        if (rs.next()) {
            return Token(
                rs.getString("data"),
                rs.getTimestamp("expires_at").toInstant(),
                rs.getString("created_by")
            )
        }
        return null
    }
}