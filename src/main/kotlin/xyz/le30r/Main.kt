package xyz.le30r

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.Chat
import com.github.kotlintelegrambot.logging.LogLevel
import com.github.kotlintelegrambot.network.fold
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.util.reflect.*
import kotlinx.coroutines.runBlocking
import xyz.le30r.dto.Message
import xyz.le30r.dto.OpenAIRequest
import xyz.le30r.dto.OpenAIResponse

@OptIn(InternalAPI::class)
suspend fun main() = runBlocking {
    val bot = bot {
        logLevel = LogLevel.All()
        token = ""
        dispatch {
            command("start") {
                val result = bot.sendMessage(chatId = message.chat.id, text = "Hi there!")
                result.fold({
                    // do something here with the response
                }, {
                    // do something with the error
                })
            }
            text {
                val client = OpenAIClient()
                runBlocking {
                    val botResponse = client.httpClient.post("https://api.openai.com/v1/chat/completions") {
                        contentType(ContentType.Application.Json)
                        setBody(OpenAIRequest("gpt-3.5-turbo", arrayOf(Message("user", text))))
                    }.body<OpenAIResponse>()
                    bot.sendMessage(chatId = message.chat.id, text = botResponse.choices[0].message.content)
                }
            }

        }

    }
    bot.startPolling()
}