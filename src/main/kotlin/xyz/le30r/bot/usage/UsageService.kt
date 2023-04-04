package xyz.le30r.bot.usage

import xyz.le30r.bot.users.token.TokenRepository


class UsageService {
    private val usageRepository = UsageRepository()

    fun initUser(userId: String) {
        if (usageRepository.find(userId) == null) {
            usageRepository.save(Usage(userId, 0, 0))
        }
    }

    fun updateUsage(usage: Usage) {
        usageRepository.update(usage)
    }

    fun getUsage(userId: String): Usage? {
        return usageRepository.find(userId)
    }


}