

import com.aliyun.fc.runtime.Context
import com.aliyun.fc.runtime.FunctionComputeLogger
import com.dingtalk.oapi.lib.aes.Utils
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.zxkane.aliyun.fc.APIRequest
import com.github.zxkane.dingtalk.AES_KEY_NAME
import com.github.zxkane.dingtalk.CORPID_NAME
import com.github.zxkane.dingtalk.EncryptedEvent
import com.github.zxkane.dingtalk.QUERY_PARAMETER_NONCE
import com.github.zxkane.dingtalk.QUERY_PARAMETER_SIGNATURE
import com.github.zxkane.dingtalk.QUERY_PARAMETER_TIMESTAMP
import com.github.zxkane.dingtalk.TOKEN_NAME
import com.nhaarman.mockitokotlin2.whenever
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.util.Collections
import org.apache.commons.codec.binary.Base64
import org.mockito.Mockito.mock

@Suppress("UNCHECKED_CAST")
@Throws(Exception::class)
fun setEnv(newenv: Map<String, String>) {
    try {
        val processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment")
        val theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment")
        theEnvironmentField.isAccessible = true
        val env = theEnvironmentField.get(null) as MutableMap<String, String>
        env.putAll(newenv)
        val theCaseInsensitiveEnvironmentField =
            processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment")
        theCaseInsensitiveEnvironmentField.isAccessible = true
        val cienv = theCaseInsensitiveEnvironmentField.get(null) as MutableMap<String, String>
        cienv.putAll(newenv)
    } catch (e: NoSuchFieldException) {
        val classes = Collections::class.java.getDeclaredClasses()
        val env = System.getenv()
        for (cl in classes) {
            if ("java.util.Collections\$UnmodifiableMap" == cl.getName()) {
                val field = cl.getDeclaredField("m")
                field.setAccessible(true)
                val obj = field.get(env)
                val map = obj as MutableMap<String, String>
                map.clear()
                map.putAll(newenv)
            }
        }
    }

}

class CallbackTests : StringSpec() {

    init {
        val token = Utils.getRandomStr(8)
        val aesKey = Utils.getRandomStr(43)
        val nonce = Utils.getRandomStr(6)
        val timestamp = System.currentTimeMillis()

        setEnv(mapOf(TOKEN_NAME to token, AES_KEY_NAME to aesKey, CORPID_NAME to "mycorp"))

        val callback = com.github.zxkane.dingtalk.Callback()
        val context = mock(Context::class.java)
        whenever(context.logger).thenReturn(mock(FunctionComputeLogger::class.java))

        callback.initialize(context)

        val objectMapper = jacksonObjectMapper()

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