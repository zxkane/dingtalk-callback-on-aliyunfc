

import com.github.zxkane.aliyun.fc.APIRequest
import com.github.zxkane.dingtalk.AES_KEY_NAME
import com.github.zxkane.dingtalk.EncryptedEvent
import com.github.zxkane.dingtalk.QUERY_PARAMETER_NONCE
import com.github.zxkane.dingtalk.QUERY_PARAMETER_SIGNATURE
import com.github.zxkane.dingtalk.QUERY_PARAMETER_TIMESTAMP
import com.github.zxkane.dingtalk.TOKEN_NAME
import io.kotlintest.shouldBe
import org.apache.commons.codec.binary.Base64

class CallbackTests : AbstractTest() {

    init {
        super.init()
        val objectMapper = callback.objectMapper

        "env init" {
            System.getenv(TOKEN_NAME) shouldBe token
            System.getenv(AES_KEY_NAME) shouldBe aesKey
        }

        "check-url callback request" {
            val encryptedMap = callback.dingTalkEncryptor.getEncryptedMap(
                """
                    {
                        "EventType" : "check_url"
                    }
                """.trimIndent(),
                timestamp, nonce)
            val encryptedMsg = objectMapper.writeValueAsString(EncryptedEvent(encryptedMap.get("encrypt") as String))

            val apiRequest = APIRequest("/callback", "POST",
                mapOf("content-type" to "application/json"),
                mapOf(
                    QUERY_PARAMETER_SIGNATURE to encryptedMap.get("msg_signature") as String,
                    QUERY_PARAMETER_NONCE to nonce,
                    QUERY_PARAMETER_TIMESTAMP to timestamp.toString()),
                mapOf(),
                Base64.encodeBase64String(encryptedMsg.toByteArray()),
                true)

            callback.handleRequest(apiRequest, context)
        }
    }
}