package com.github.zxkane.dingtalk

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.ZonedDateTime

data class EncryptedEvent(
    @JsonProperty(required = true) val encrypt: String
)

open class Event(val eventType: String)

data class BPMEvent(
    @JsonProperty("EventType", required = true) val type: String,
    @JsonProperty("processInstanceId") val instanceId: String?,
    @JsonProperty("corpId") val corpId: String?,
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    @JsonProperty("createTime") val createdTime: ZonedDateTime?,
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    @JsonProperty("finishTime") val finishTime: ZonedDateTime?,
    @JsonProperty("bizCategoryId") val categoryId: String?,
    @JsonProperty("title") val title: String?,
    @JsonProperty("staffId") val staff: String?,
    @JsonProperty("result") val result: String?,
    @JsonProperty("remark") val remark: String?,
    @JsonProperty("url") val url: String?
)
    : Event(type) {

    constructor(type: String) : this(type, null, null, null, null,
        null, null, null, null, null, null)
}
