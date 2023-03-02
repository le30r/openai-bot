package xyz.le30r.client

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
import xyz.le30r.Application
import xyz.le30r.dto.Message
import xyz.le30r.dto.OpenAIRequest
import xyz.le30r.dto.OpenAIResponse

class OpenAIClient {
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

    suspend fun nextMessage(history: List<Message>): Message =
        httpClient.post("https://api.openai.com/v1/chat/completions") {
            contentType(ContentType.Application.Json)
            setBody(OpenAIRequest("gpt-3.5-turbo", messages = history))
        }.body<OpenAIResponse>().choices[0].message

    suspend fun nextMessageDebug(history: List<Message>): Message {
        val text = httpClient.post("https://api.openai.com/v1/chat/completions") {
            contentType(ContentType.Application.Json)
            setBody(OpenAIRequest("gpt-3.5-turbo-0301", messages = history))
        }.bodyAsText()
        println(text)
        return Message("", "")
    }

}