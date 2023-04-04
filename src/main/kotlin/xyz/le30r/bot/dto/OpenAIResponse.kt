package xyz.le30r.bot.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenAIResponse(val id:String,
                          @SerialName("object")
                          val chatObject:String,
                          val created:Int,
                          val choices:Array<Choice>,
                          val usage: UsageDto) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OpenAIResponse

        if (id != other.id) return false
        if (chatObject != other.chatObject) return false
        if (created != other.created) return false
        if (!choices.contentEquals(other.choices)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + chatObject.hashCode()
        result = 31 * result + created
        result = 31 * result + choices.contentHashCode()
        return result
    }
}
