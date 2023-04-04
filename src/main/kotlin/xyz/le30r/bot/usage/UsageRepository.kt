package xyz.le30r.bot.usage

import xyz.le30r.bot.Application
import java.sql.Connection
import java.sql.DriverManager

class UsageRepository {
    private val connection: Connection = DriverManager.getConnection(Application.Secrets.jdbcUrl)
    fun update(entity: Usage) {
        val statement = connection.prepareStatement(
            """UPDATE usage 
            | SET prompt_tokens = prompt_tokens + ?, completion_tokens = completion_tokens + ?
            | WHERE user_id = ?""".trimMargin()
        )

        statement.setInt(1, entity.promptTokens)
        statement.setInt(2, entity.completionTokens)
        statement.setString(3, entity.userId)
        statement.executeUpdate()
    }

    fun save(entity: Usage) {
        val statement = connection.prepareStatement(
            """INSERT INTO usage (user_id, prompt_tokens, completion_tokens)  
            | VALUES (?, 0, 0)
        """.trimMargin()
        )
        statement.setString(1, entity.userId)
        statement.executeUpdate()
    }

    fun find(userId: String): Usage? {
        val statement = connection.prepareStatement(
            """SELECT user_id, prompt_tokens, completion_tokens 
            | FROM usage
            | WHERE user_id = ?
        """.trimMargin()
        )
        statement.setString(1, userId)
        val rs = statement.executeQuery()

        if (rs.next()) {
            return Usage(
                rs.getString("user_id"),
                rs.getInt("prompt_tokens"),
                rs.getInt("completion_tokens")
            )
        }
        return null
    }
}