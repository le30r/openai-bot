package xyz.le30r.bot.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import xyz.le30r.bot.Application
import xyz.le30r.bot.dto.Message
import xyz.le30r.bot.dto.OpenAIRequest
import xyz.le30r.bot.dto.OpenAIResponse

class OpenAIClient {
    val logger = LoggerFactory.getLogger(Application::class.java)

    val httpClient = HttpClient(Java) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
        install(Auth) {
            bearer {
                loadTokens {
                    BearerTokens(Application.Secrets.openAiToken, "")
                }
            }
        }
    }

    suspend fun nextMessage(history: List<Message>): OpenAIResponse {
        val request = httpClient.post("https://api.openai.com/v1/chat/completions") {
            contentType(ContentType.Application.Json)
            setBody(OpenAIRequest("gpt-3.5-turbo-0613", messages = history))
        }
        logger.debug(request.requestTime.toString())
        return request.body()
    }


    suspend fun nextMessageDebug(history: List<Message>): Message {
        val text = httpClient.post("https://api.openai.com/v1/chat/completions") {
            contentType(ContentType.Application.Json)
            setBody(OpenAIRequest("gpt-3.5-turbo-0613", messages = history))
        }.bodyAsText()
        println(text)
        return Message("", "")
    }

}