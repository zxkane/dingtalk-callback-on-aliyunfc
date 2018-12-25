
import com.alicloud.openservices.tablestore.SyncClient
import com.aliyun.fc.runtime.Context
import com.aliyun.fc.runtime.Credentials
import com.aliyun.fc.runtime.FunctionComputeLogger
import com.dingtalk.oapi.lib.aes.Utils
import com.github.zxkane.dingtalk.AES_KEY_NAME
import com.github.zxkane.dingtalk.CORPID_NAME
import com.github.zxkane.dingtalk.DTS_ENDPOINT
import com.github.zxkane.dingtalk.DTS_INSTANCE_NAME
import com.github.zxkane.dingtalk.TOKEN_NAME
import com.nhaarman.mockitokotlin2.whenever
import io.kotlintest.specs.StringSpec
import org.mockito.Mockito
import org.mockito.Mockito.mock
import java.util.Collections


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

abstract class AbstractTest : StringSpec() {

    val callback = com.github.zxkane.dingtalk.Callback()
    val context = Mockito.mock(Context::class.java)
    val token = Utils.getRandomStr(8)
    val aesKey = Utils.getRandomStr(43)
    val nonce = Utils.getRandomStr(6)
    val timestamp = System.currentTimeMillis()

    fun init() {
        setEnv(mapOf(TOKEN_NAME to token, AES_KEY_NAME to aesKey, CORPID_NAME to "mycorp",
            DTS_ENDPOINT to "https://dingtalk.cn-beijing.ots.aliyuncs.com",
            DTS_INSTANCE_NAME to "myinstance"))
        whenever(context.logger).thenReturn(mock(FunctionComputeLogger::class.java))
        val credentials = mock(Credentials::class.java)
        whenever(context.executionCredentials).thenReturn(credentials)
        whenever(credentials.accessKeyId).thenReturn(Utils.getRandomStr(8))
        whenever(credentials.accessKeySecret).thenReturn(Utils.getRandomStr(12))
        whenever(credentials.securityToken).thenReturn(Utils.getRandomStr(12))
        callback.initialize(context)
        callback.syncClient = mock(SyncClient::class.java)
    }
}