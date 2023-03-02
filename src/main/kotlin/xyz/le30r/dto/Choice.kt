package xyz.le30r.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Choice(val index:Int,
                  val message:Message,
                  @SerialName("finish_reason")
                  val finishReason:String?)
