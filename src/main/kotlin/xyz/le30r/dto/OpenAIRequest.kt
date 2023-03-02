package xyz.le30r.dto

import kotlinx.serialization.Serializable

@Serializable
data class OpenAIRequest(val model:String,
                         val messages:Array<Message>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OpenAIRequest

        if (model != other.model) return false
        if (!messages.contentEquals(other.messages)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = model.hashCode()
        result = 31 * result + messages.contentHashCode()
        return result
    }
}
