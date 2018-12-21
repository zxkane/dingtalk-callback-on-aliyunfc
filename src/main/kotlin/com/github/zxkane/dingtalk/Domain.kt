package com.github.zxkane.dingtalk

import com.fasterxml.jackson.annotation.*
import java.time.ZonedDateTime

data class EncryptedEvent(
    @JsonProperty(required = true) val encrypt: String
)

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "EventType", visible = true)
@JsonSubTypes(
    JsonSubTypes.Type(value = Event.CheckEvent::class, name = "check_url"),
    JsonSubTypes.Type(value = Event.OrgEvent::class, name = "user_add_org"),
    JsonSubTypes.Type(value = Event.OrgEvent::class, name = "user_modify_org"),
    JsonSubTypes.Type(value = Event.OrgEvent::class, name = "user_leave_org"),
    JsonSubTypes.Type(value = Event.OrgEvent::class, name = "org_admin_add"),
    JsonSubTypes.Type(value = Event.OrgEvent::class, name = "org_admin_remove"),
    JsonSubTypes.Type(value = Event.OrgEvent::class, name = "org_dept_create"),
    JsonSubTypes.Type(value = Event.OrgEvent::class, name = "org_dept_modify"),
    JsonSubTypes.Type(value = Event.OrgEvent::class, name = "org_dept_remove"),
    JsonSubTypes.Type(value = Event.OrgEvent::class, name = "org_change"),
    JsonSubTypes.Type(value = Event.BPMEvent::class, name = "bpms_task_change"),
    JsonSubTypes.Type(value = Event.BPMEvent::class, name = "bpms_instance_change")
)
sealed class Event(val type: String) {

    data class CheckEvent(@JsonProperty("EventType", required = true) val eventType: String) : Event(eventType)

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class OrgEvent(
        @JsonProperty("EventType", required = true)
        val eventType: String,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        @JsonProperty("TimeStamp", required = true)
        val timeStamp: ZonedDateTime,
        @JsonProperty("UserId")
        val userIds: List<String>?,
        @JsonProperty("DeptId")
        val departmentIds: List<String>?,
        @JsonProperty("CorpId")
        val corpId: String?
    ) : Event(eventType)

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class BPMEvent(
        @JsonProperty("EventType", required = true)
        val eventType: String,
        @JsonProperty("taskId", required = false)
        val taskId: Long,
        @JsonProperty("processCode", required = false)
        val processCode: String?,
        val processInstanceId: String,
        val corpId: String,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        @JsonProperty("createTime", required = true)
        val createdTime: ZonedDateTime,
        @JsonProperty("finishTime")
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        val finishedTime: ZonedDateTime?,
        @JsonProperty("bizCategoryId", required = true)
        val categoryId: String,
        val title: String,
        @JsonProperty("type", required = true)
        val bpmType: String,
        val staffId: String,
        val result: String?,
        val remark: String?,
        val url: String?
    ) : Event(eventType)
}
