package xyz.le30r.bot.dto

import kotlinx.serialization.Serializable

@Serializable
data class Message(val role:String, val content:String)
