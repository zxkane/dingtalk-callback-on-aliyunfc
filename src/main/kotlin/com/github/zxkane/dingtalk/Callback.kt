package com.github.zxkane.dingtalk

import com.aliyun.fc.runtime.Context
import com.aliyun.fc.runtime.FunctionInitializer
import com.aliyun.fc.runtime.PojoRequestHandler
import com.dingtalk.oapi.lib.aes.DingTalkEncryptor
import com.dingtalk.oapi.lib.aes.Utils
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.zxkane.aliyun.fc.APIRequest
import com.github.zxkane.aliyun.fc.APIResponse
import org.apache.commons.codec.binary.Base64

const val TOKEN_NAME = "DD_TOKEN"
const val AES_KEY_NAME = "DD_AES_KEY"
const val CORPID_NAME = "DD_CORPID"

const val RESPONSE_MSG = "success"

class Callback : PojoRequestHandler<APIRequest, APIResponse>, FunctionInitializer {

    lateinit var objectMapper: ObjectMapper
    lateinit var dingTalkEncryptor: DingTalkEncryptor

    override fun initialize(context: Context?) {
        objectMapper = ObjectMapper().registerModules(JavaTimeModule()).registerKotlinModule()
        dingTalkEncryptor = DingTalkEncryptor(System.getenv(TOKEN_NAME),
            System.getenv(AES_KEY_NAME),
            System.getenv(CORPID_NAME))
    }

    override fun handleRequest(request: APIRequest, context: Context): APIResponse {
        val logger = context.logger
        logger.debug("Callback request is $request")

        val encryptedEvent = objectMapper.readValue<EncryptedEvent>(
            if (request.isBase64Encoded) String(Base64.decodeBase64(request.body)) else request.body,
            EncryptedEvent::class.java
        )

        logger.debug("Encrypted callback event is $encryptedEvent.")

        val eventJson = dingTalkEncryptor.getDecryptMsg(request.queryParameters.get("signature"),
                request.queryParameters.get("timestamp"), request.queryParameters.get("nonce"),
                encryptedEvent.encrypt)

        logger.debug("Event json is $eventJson.")

        val bpmEvent = objectMapper.readValue<BPMEvent>(eventJson, BPMEvent::class.java)

        when (bpmEvent.eventType) {
            "check_url" -> {
                logger.debug("Received callback validation request.")
            }
            "bpms_instance_change" -> {
                logger.debug("BPM $bpmEvent is started or finished.")
            }
            "bpms_task_change" -> {
                logger.debug("BPM $bpmEvent is changed.")
            }
        }

        val response = dingTalkEncryptor.getEncryptedMap(RESPONSE_MSG, System.currentTimeMillis(),
            Utils.getRandomStr(12))

        logger.debug("Callback response is $response.")

        return APIResponse(Base64.encodeBase64String(objectMapper.writeValueAsString(response).toByteArray()),
            mapOf("content-type" to "application/json"), true, 200)
    }
}