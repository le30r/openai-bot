package xyz.le30r.bot.usage

data class Usage (val userId: String, val promptTokens: Int, val completionTokens: Int)