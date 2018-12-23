package com.github.zxkane.dingtalk

import com.alicloud.openservices.tablestore.SyncClient
import com.alicloud.openservices.tablestore.model.Column
import com.alicloud.openservices.tablestore.model.ColumnValue
import com.alicloud.openservices.tablestore.model.PrimaryKeyBuilder
import com.alicloud.openservices.tablestore.model.PrimaryKeyValue
import com.alicloud.openservices.tablestore.model.PutRowRequest
import com.alicloud.openservices.tablestore.model.RowPutChange
import com.aliyun.fc.runtime.Context
import com.aliyun.fc.runtime.FunctionComputeLogger
import com.aliyun.fc.runtime.FunctionInitializer
import com.aliyun.fc.runtime.PojoRequestHandler
import com.dingtalk.oapi.lib.aes.DingTalkEncryptor
import com.dingtalk.oapi.lib.aes.Utils
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.zxkane.aliyun.fc.APIRequest
import com.github.zxkane.aliyun.fc.APIResponse
import java.time.ZonedDateTime
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaType
import org.apache.commons.codec.binary.Base64

const val TOKEN_NAME = "DD_TOKEN"
const val AES_KEY_NAME = "DD_AES_KEY"
const val CORPID_NAME = "DD_CORPID"

const val QUERY_PARAMETER_SIGNATURE = "signature"
const val QUERY_PARAMETER_TIMESTAMP = "timestamp"
const val QUERY_PARAMETER_NONCE = "nonce"

const val DTS_ENDPOINT = "DTS_ENDPOINT"
const val DTS_ACCESS_KEY = "DTS_ACCESS_KEY"
const val DTS_KEY_SECRET = "DTS_KEY_SECRET"
const val DTS_INSTANCE_NAME = "DTS_INSTANCE_NAME"

const val BPM_TABLE_NAME = "bpm_raw"
const val BPM_TABLE_PRIMARY_KEY_NAME = "processInstanceId"

const val ORG_TABLE_NAME = "org_raw"
const val ORG_TABLE_PRIMARY_KEY_NAME = "eventType"

const val RESPONSE_MSG = "success"

const val NONCE_LENGTH = 12
const val STATUS_CODE = 200

class Callback : PojoRequestHandler<APIRequest, APIResponse>, FunctionInitializer {

    lateinit var objectMapper: ObjectMapper
    lateinit var dingTalkEncryptor: DingTalkEncryptor
    lateinit var syncClient: SyncClient

    override fun initialize(context: Context?) {
        objectMapper = ObjectMapper().registerModules(JavaTimeModule()).registerKotlinModule()
        objectMapper.disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
        dingTalkEncryptor = DingTalkEncryptor(System.getenv(TOKEN_NAME),
            System.getenv(AES_KEY_NAME),
            System.getenv(CORPID_NAME))
        syncClient = SyncClient(System.getenv(DTS_ENDPOINT), System.getenv(DTS_ACCESS_KEY),
            System.getenv(DTS_KEY_SECRET), System.getenv(DTS_INSTANCE_NAME))
    }

    override fun handleRequest(request: APIRequest, context: Context): APIResponse {
        val logger = context.logger
        logger.debug("Callback request is $request")

        val encryptedEvent = objectMapper.readValue<EncryptedEvent>(
            if (request.isIsBase64Encoded) String(Base64.decodeBase64(request.body)) else request.body,
            EncryptedEvent::class.java
        )

        logger.debug("Encrypted callback event is $encryptedEvent.")

        val eventJson = dingTalkEncryptor.getDecryptMsg(
            request.queryParameters.get(QUERY_PARAMETER_SIGNATURE),
            request.queryParameters.get(QUERY_PARAMETER_TIMESTAMP),
            request.queryParameters.get(QUERY_PARAMETER_NONCE),
            encryptedEvent.encrypt)

        logger.debug("Event json is $eventJson.")

        val event = objectMapper.readValue<Event>(eventJson, Event::class.java)

        when (event.type) {
            "check_url" -> {
                logger.debug("Received callback validation request.")
            }
            "bpms_instance_change", "bpms_task_change" -> {
                logger.debug("BPM $event is received.")
                serializeEvent(event, context.logger)
            }
            "user_add_org", "user_modify_org", "user_leave_org", "org_admin_add",
                "org_admin_remove", "org_dept_create", "org_dept_modify", "org_dept_remove",
                "org_change" -> {
                logger.debug("Org event $event is received.")
            }
            else -> {
                logger.debug("Unrecognized event $event is received.")
            }
        }

        val response = dingTalkEncryptor.getEncryptedMap(RESPONSE_MSG, System.currentTimeMillis(),
            Utils.getRandomStr(NONCE_LENGTH))

        logger.debug("Callback response is $response.")

        return APIResponse(Base64.encodeBase64String(objectMapper.writeValueAsString(response).toByteArray()),
            mapOf("content-eventType" to "application/json"), true, STATUS_CODE)
    }

    @Suppress("DEPRECATION")
    fun serializeEvent(
        event: Event,
        tableName: String,
        primaryKeyName: String,
        logger: FunctionComputeLogger
    ): RowPutChange {
        var rowPutChange: RowPutChange? = null
        val columns: MutableList<Column> = mutableListOf()

        event.javaClass.kotlin.declaredMemberProperties.forEach { prop ->
            if (prop.name == primaryKeyName) {
                val primaryKeyBuilder = PrimaryKeyBuilder.createPrimaryKeyBuilder()
                primaryKeyBuilder.addPrimaryKeyColumn(primaryKeyName,
                    PrimaryKeyValue.fromString(prop.get(event) as String))
                val primaryKey = primaryKeyBuilder.build()

                rowPutChange = RowPutChange(tableName, primaryKey)
            } else {
                when (prop.returnType.javaType.typeName) {
                    String::class.java.typeName -> {
                        val value = prop.get(event) as String?
                        columns.add(Column(prop.name, ColumnValue.fromString(value ?: "null")))
                    }
                    "java.util.List<java.lang.String>" -> {
                        val value = prop.get(event) as List<*>?
                        val strValue = value?.joinToString()
                        columns.add(Column(prop.name, ColumnValue.fromString(strValue ?: "null")))
                    }
                    Long::class.java.typeName -> {
                        columns.add(Column(prop.name, ColumnValue.fromLong(prop.get(event) as Long)))
                    }
                    ZonedDateTime::class.java.typeName -> {
                        val value = prop.get(event) as ZonedDateTime?
                        columns.add(Column(prop.name, ColumnValue.fromString(
                            value?.toLocalDateTime().toString())))
                    }
                    else ->
                        logger.warn("Unrecognized prop '${prop.name}' with type ${prop.returnType}.")
                }
            }
        }

        columns.forEach { column -> rowPutChange!!.addColumn(column) }
        return rowPutChange!!
    }

    private fun serializeEvent(event: Event, logger: FunctionComputeLogger) {
        var rowPutChange: RowPutChange? = null
        when (event::class) {
            Event.BPMEvent::class -> {
                rowPutChange = serializeEvent(event, BPM_TABLE_NAME, BPM_TABLE_PRIMARY_KEY_NAME, logger)
            }
            Event.OrgEvent::class -> {
                rowPutChange = serializeEvent(event, ORG_TABLE_NAME, ORG_TABLE_PRIMARY_KEY_NAME, logger)
            }
        }
        syncClient.putRow(PutRowRequest(rowPutChange!!))
    }
}
