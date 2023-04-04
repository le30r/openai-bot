package xyz.le30r.bot.users

class UserService {
    private val repository = UserRepository()

    fun isUserPresented(user: String): Boolean = repository.find(user) != null
    fun getUserLevel(user: String): Int? = repository.find(user)?.level

    fun addUser(user: String, level: Int): Int {
        return if (repository.find(user) == null) {
            repository.save(User(user, level))
        } else 0
    }
}