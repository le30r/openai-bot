package xyz.le30r.bot

import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.behaviour_builder.telegramBotWithBehaviourAndLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommandWithArgs
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onContentMessage
import dev.inmo.tgbotapi.extensions.utils.chatIdOrNull
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.text
import dev.inmo.tgbotapi.extensions.utils.shortcuts.executeAsync
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.ChatIdentifier
import dev.inmo.tgbotapi.types.message.Markdown
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.MessageContent
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.utils.RiskFeature
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.le30r.bot.Application.Secrets.tgToken
import xyz.le30r.bot.client.OpenAIClient
import xyz.le30r.bot.dto.Message
import xyz.le30r.bot.usage.Usage
import xyz.le30r.bot.usage.UsageService
import xyz.le30r.bot.users.UserService
import xyz.le30r.bot.users.token.TokenService
import java.time.LocalDate

fun main() {
    Application().run()
}

class Application {
    val logger: Logger = LoggerFactory.getLogger(Application::class.java)
    object Secrets {
        val openAiToken: String = System.getenv("OAI_TOKEN")
        val tgToken: String = System.getenv("TG_TOKEN")
        val jdbcUrl = System.getenv("DB_URL")
    }

    private lateinit var bot: TelegramBot
    private val client = OpenAIClient()
    private var history: MutableMap<Long, MutableList<Message>> = mutableMapOf()
    private val userService = UserService()
    private val tokenService = TokenService()
    private val usageService = UsageService()

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

            onCommand("token") {
                generateToken(it.chat.id)
                return@onCommand
            }

            onCommand("usage") {
                getUsage(it)
                return@onCommand
            }

            onCommandWithArgs("auth", { true }) { commonMessage, strings ->
                activate(commonMessage, strings)
                return@onCommandWithArgs
            }

            onContentMessage {
                println(it.text ?: "")
                if ((it.text?.get(0) ?: "") != '/') {
                    processMessage(it)
                }
                return@onContentMessage
            }


        }
        bot = botPair.first
        botPair.second.join()

    }

    private suspend fun getUsage(it: CommonMessage<TextContent>) {
        val chatId = it.chat.id
        val userId = chatId.chatId.toString()
        val usage = usageService.getUsage(userId)
        var text = "You have not used the service yet"
        if (usage != null) {
            text = """
            **Usage statistics:**
            Prompt tokens: ${usage.promptTokens}
            Completion tokens: ${usage.completionTokens}
            Total: ${usage.promptTokens + usage.completionTokens}
            
            Cost: $${String.format("%.3f", (((usage.promptTokens + usage.completionTokens) / 1000.0) * 0.002))}
        """.trimIndent()
        } else {
            usageService.initUser(userId)
        }
        bot.executeAsync(SendTextMessage(chatId, text, parseMode = Markdown))
    }

    private suspend fun activate(it: CommonMessage<TextContent>, strings: Array<String>) {
        val token = strings[0]
        val id = it.chat.id
        if (tokenService.isValidToken(token)) {
            if (userService.addUser(id.chatId.toString(), 0) != 0) {
                usageService.initUser(id.chatId.toString())
                bot.executeAsync(SendTextMessage(id, "Accessed"))
            } else {
                bot.executeAsync(SendTextMessage(id, "You already have access"))
            }
        } else {
            bot.executeAsync(SendTextMessage(id, "Invalid token"))
        }
    }

    private suspend fun generateToken(id: ChatIdentifier) {
        val userId = id.chatIdOrNull()?.chatId.toString()
        if (((userService.getUserLevel(userId)) ?: 0) == 1) {
            val token = tokenService.generateToken(userId)
            bot.executeAsync(SendTextMessage(id, "${token.data} expires at ${token.expiresAt}"))
        } else {
            bot.executeAsync(SendTextMessage(id, "You do not have permissions"))
        }
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
        if (!userService.isUserPresented(message.from?.id?.chatId.toString() ?: "")) {
            bot.execute(SendTextMessage(message.chat.id, "Sorry, you are not in whitelist"))
            return@runBlocking
        }

        val chatId = message.chat.id.chatId
        if (message.text == null) return@runBlocking
        if (history[chatId] == null) {
            bot.execute(SendTextMessage(message.chat.id, "Type /start to start dialogue"))
            return@runBlocking
        }
        val userMessage = message.text
        history[chatId]!!.add(Message("user", userMessage ?: return@runBlocking))
        launch {
            logger.debug("Executing request to OpenAI API")
            val botResponse = client.nextMessage(history[chatId]!!)
            logger.debug("Request execution is completed")
            val resultMessage = botResponse.choices[0].message
            history[chatId]!!.add(resultMessage)
            usageService.updateUsage(
                Usage(
                    chatId.toString(),
                    botResponse.usage.completionTokens,
                    botResponse.usage.promptTokens
                )
            )
            bot.execute(SendTextMessage(message.chat.id, resultMessage.content, parseMode = Markdown))

            if (botResponse.usage.promptTokens + botResponse.usage.completionTokens >= 4096 * 0.9f) {
                bot.execute(SendTextMessage(message.chat.id, "90% of the current conversation tokens have been used. " +
                        "After a few messages, the bot may stop responding"))
            }
        }


    }

    private fun clearHistory(id: Long) {

        history[id] = mutableListOf(
            Message(
                "system",
                "You are ChatGPT, a large language model trained by OpenAI. Answer as concisely as possible. " + "Knowledge cutoff: ${LocalDate.now()}. Today is ${LocalDate.now()}." + " Your creator is Ivan Marinin"
            )
        )
    }
}