package xyz.le30r

import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class OpenAIClient {
    val httpClient = HttpClient(Java) {
        install(ContentNegotiation) {
            json(Json {
              prettyPrint =true
              ignoreUnknownKeys = true
            })
        }
        install(Auth) {
            bearer {
                loadTokens {

                    BearerTokens("", "")
                }
            }
        }
    }
}