package xyz.le30r

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.extensions.filters.Filter
import com.github.kotlintelegrambot.logging.LogLevel
import escapedMsg
import kotlinx.coroutines.runBlocking
import xyz.le30r.client.OpenAIClient
import xyz.le30r.dto.Message
import java.time.LocalDate
import java.util.*

fun main() {
    Application().run()
}

class Application {

    object Secrets {
        val openAiToken: String = System.getenv("OAI_TOKEN")
        val tgToken: String = System.getenv("TG_TOKEN")
    }

    private val client = OpenAIClient()
    private var history: MutableMap<Long, MutableList<Message>> = mutableMapOf()
    fun run() = runBlocking {

        val bot = bot {
            logLevel = LogLevel.All()
            token = Secrets.tgToken
            dispatch {
                command("start") {
                    val result = bot.sendMessage(chatId = message.chat.id, text = "Hi there!")
                    clearHistory(message.chat.id)
                }

                command("new") {
                    val result = bot.sendMessage(chatId = message.chat.id, text = "You have created a new dialogue")
                    clearHistory(message.chat.id)
                }

                message(!Filter.Command) {
                    if (message.text == null) return@message
                    if (history[message.chat.id] == null) {
                        bot.sendMessage(chatId = message.chat.id, text = "Type /start to start conversation")
                        return@message
                    }
                    val userMessage = message.text
                    history[message.chat.id]!!.add(Message("user", userMessage ?: return@message))
                    runBlocking {
                        val botResponse = client.nextMessage(history[message.chat.id]!!)
                        history[message.chat.id]!!.add(botResponse)
                        bot.sendMessage(chatId = message.chat.id, text = botResponse.content, parseMode = ParseMode.MARKDOWN);
                    }
                }
            }
        }
        bot.startPolling()
    }

    private fun clearHistory(id: Long) {
        history[id] = mutableListOf(
            Message("system",
                "You are ChatGPT, a large language model trained by OpenAI. Answer as concisely as possible. " +
                        "Knowledge cutoff: ${LocalDate.now()}. Your creator is Ivan Marinin")
        )
    }
}