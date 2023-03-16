package xyz.le30r

import dev.inmo.micro_utils.coroutines.subscribeSafelyWithoutExceptions
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.telegramBotWithBehaviourAndLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onContentMessage
import dev.inmo.tgbotapi.extensions.utils.chatIdOrNull
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.text
import dev.inmo.tgbotapi.extensions.utils.shortcuts.executeAsync
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.ChatIdentifier
import dev.inmo.tgbotapi.types.Username
import dev.inmo.tgbotapi.types.message.Markdown
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.MessageContent
import dev.inmo.tgbotapi.types.message.textsources.mention
import dev.inmo.tgbotapi.utils.RiskFeature
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import xyz.le30r.Application.Secrets.tgToken
import xyz.le30r.client.OpenAIClient
import xyz.le30r.dto.Message
import java.time.LocalDate

fun main() {
    Application().run()
}

class Application {

    object Secrets {
        val openAiToken: String = System.getenv("OAI_TOKEN")
        val tgToken: String = System.getenv("TG_TOKEN")
    }

    private lateinit var bot: TelegramBot
    private val client = OpenAIClient()
    private var history: MutableMap<Long, MutableList<Message>> = mutableMapOf()
    @OptIn(RiskFeature::class)
    fun run() = runBlocking {

        val botPair = telegramBotWithBehaviourAndLongPolling(tgToken, CoroutineScope(Dispatchers.IO)) {
            onCommand("start") {
                commandStart(it.chat.id)
                return@onCommand
            }

            onCommand("new") {
                newChat(it.chat.id)
                return@onCommand
            }

            onContentMessage {
                println(it.text ?: "")
                if ((it.text?.get(0) ?: "") != '/' ) {
                    processMessage(it)
                }
                return@onContentMessage
            }


        }
        bot = botPair.first
        botPair.second.join()

    }

    private suspend fun commandStart(id: ChatIdentifier) {
        bot.executeAsync(SendTextMessage(id, "Hello there!"))
        clearHistory(id.chatIdOrNull()?.chatId ?: 0)
    }

    private suspend fun newChat(id: ChatIdentifier) {
        bot.executeAsync(SendTextMessage(id, "Hello there!"))
        clearHistory(id.chatIdOrNull()?.chatId ?: 0)
    }

    private suspend fun processMessage(message: CommonMessage<MessageContent>): Unit = runBlocking {
        val chatId = message.chat.id.chatId
        if (message.text == null) return@runBlocking
        if (history[chatId] == null) {
            bot.execute(SendTextMessage(message.chat.id, "Type /start to start dialogue"))
            return@runBlocking
        }
        val userMessage = message.text
        history[chatId]!!.add(Message("user", userMessage ?: return@runBlocking))
        launch {
            val botResponse = client.nextMessage(history[chatId]!!)
            history[chatId]!!.add(botResponse)
            bot.execute(SendTextMessage(message.chat.id, botResponse.content, parseMode = Markdown))
        }


    }

    private fun clearHistory(id: Long) {

        history[id] = mutableListOf(
            Message(
                "system",
                "You are ChatGPT, a large language model trained by OpenAI. Answer as concisely as possible. " +
                        "Knowledge cutoff: ${LocalDate.now()}. Today is ${LocalDate.now()}." +
                        " Your creator is Ivan Marinin"
            )
        )
    }
}