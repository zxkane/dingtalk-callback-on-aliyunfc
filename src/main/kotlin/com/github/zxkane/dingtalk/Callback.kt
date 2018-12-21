package com.github.zxkane.dingtalk

import com.aliyun.fc.runtime.Context
import com.aliyun.fc.runtime.FunctionInitializer
import com.aliyun.fc.runtime.StreamRequestHandler
import com.dingtalk.oapi.lib.aes.DingTalkEncryptor
import com.dingtalk.oapi.lib.aes.Utils
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.zxkane.aliyun.fc.APIRequest
import com.github.zxkane.aliyun.fc.APIResponse
import java.io.InputStream
import java.io.OutputStream
import org.apache.commons.codec.binary.Base64

const val TOKEN_NAME = "DD_TOKEN"
const val AES_KEY_NAME = "DD_AES_KEY"
const val CORPID_NAME = "DD_CORPID"

const val QUERY_PARAMETER_SIGNATURE = "signature"
const val QUERY_PARAMETER_TIMESTAMP = "timestamp"
const val QUERY_PARAMETER_NONCE = "nonce"

const val RESPONSE_MSG = "success"

const val NONCE_LENGTH = 12
const val STATUS_CODE = 200

class Callback : /* PojoRequestHandler<APIRequest, APIResponse>, */ StreamRequestHandler, FunctionInitializer {

    lateinit var objectMapper: ObjectMapper
    lateinit var dingTalkEncryptor: DingTalkEncryptor

    override fun initialize(context: Context?) {
        objectMapper = ObjectMapper().registerModules(JavaTimeModule()).registerKotlinModule()
        dingTalkEncryptor = DingTalkEncryptor(System.getenv(TOKEN_NAME),
            System.getenv(AES_KEY_NAME),
            System.getenv(CORPID_NAME))
    }

    fun handleRequest(request: APIRequest, context: Context): APIResponse {
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

    override fun handleRequest(input: InputStream, output: OutputStream, context: Context) {
        val request = objectMapper.readValue<APIRequest>(input)

        val response = handleRequest(request, context)

        objectMapper.writeValue(output, response)
    }
}
