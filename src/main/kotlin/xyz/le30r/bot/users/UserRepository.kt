package xyz.le30r.bot.users

import xyz.le30r.bot.Application
import java.sql.Connection
import java.sql.DriverManager

class UserRepository {
    private val connection: Connection = DriverManager.getConnection(Application.Secrets.jdbcUrl)

    fun save(entity: User): Int {
        val statement = connection.prepareStatement("INSERT INTO users VALUES (?, ?)")
        statement.setString(1, entity.id)
        statement.setInt(2, entity.level)
        return statement.executeUpdate()
    }

    fun find(user: String): User? {
        val statement = connection.prepareStatement("""SELECT "user", level FROM users WHERE "user" = ?""")
        statement.setString(1, user)
        val rs = statement.executeQuery()
        if (rs.next()) {
            return User(
                rs.getString("user"),
                rs.getInt("level")
            )
        }
        return null
    }
}