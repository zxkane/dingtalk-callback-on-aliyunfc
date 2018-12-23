

import com.github.zxkane.aliyun.fc.APIRequest
import com.github.zxkane.dingtalk.AES_KEY_NAME
import com.github.zxkane.dingtalk.BPM_TABLE_NAME
import com.github.zxkane.dingtalk.BPM_TABLE_PRIMARY_KEY_NAME
import com.github.zxkane.dingtalk.EncryptedEvent
import com.github.zxkane.dingtalk.Event
import com.github.zxkane.dingtalk.ORG_TABLE_NAME
import com.github.zxkane.dingtalk.ORG_TABLE_PRIMARY_KEY_NAME
import com.github.zxkane.dingtalk.QUERY_PARAMETER_NONCE
import com.github.zxkane.dingtalk.QUERY_PARAMETER_SIGNATURE
import com.github.zxkane.dingtalk.QUERY_PARAMETER_TIMESTAMP
import com.github.zxkane.dingtalk.TOKEN_NAME
import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.tables.row
import org.apache.commons.codec.binary.Base64
import java.time.ZonedDateTime

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

        "serialize bpm event" {
            forall(
                row(Event.BPMEvent("bpmEvent", 0, "code-xxx",
                    "instance-yyyy", "corpId", ZonedDateTime.now(), ZonedDateTime.now(), "categoryId",
                    "title", "bpm-type", "staff-22222", null, "222", "http://11.com")),
                row(Event.BPMEvent("bpmEvent", 0, "code-xxx",
                    "instance-yyyy", "corpId", ZonedDateTime.now(), null, "categoryId",
                    "title", "bpm-type", "staff-22222", null, null, null))
            ) { event ->
                val putRowChange = callback.serializeEvent(event, BPM_TABLE_NAME, BPM_TABLE_PRIMARY_KEY_NAME,
                    context.logger)
                putRowChange.has("processInstanceId") shouldBe true
                putRowChange.has("taskId") shouldBe true
                putRowChange.primaryKey.contains(BPM_TABLE_PRIMARY_KEY_NAME) shouldBe true
            }
        }

        "serialize org event" {
            forall(
                row(Event.OrgEvent("org-event", ZonedDateTime.now(),
                    null, null, "corpid"))
            ) { event ->
                val putRowChange = callback.serializeEvent(event, ORG_TABLE_NAME, ORG_TABLE_PRIMARY_KEY_NAME,
                    context.logger)
                putRowChange.has("processInstanceId") shouldBe false
                putRowChange.has("corpId") shouldBe true
                putRowChange.columnsToPut.size shouldBe 4
                putRowChange.primaryKey.contains(ORG_TABLE_PRIMARY_KEY_NAME) shouldBe true
            }
        }
    }
}