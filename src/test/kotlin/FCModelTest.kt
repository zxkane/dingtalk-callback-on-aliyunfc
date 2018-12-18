
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.zxkane.aliyun.fc.APIRequestKt
import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.StringSpec
import io.kotlintest.tables.row
import java.io.ByteArrayInputStream

class FCModelTests : StringSpec() {

    init {
        val objectMapper = ObjectMapper()
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)

        "can deserialize API Request" {
            forall(
                    row("{ \"path\":\"api request path\", \"httpMethod\":\"request method name\", \"headers\":{}, \"queryParameters\":{}, \"pathParameters\":{}, \"body\":\"string of request payload\", \"isBase64Encoded\":true }"),
                    row("{ \"path\":\"api request path\", \"httpMethod\":\"request method name\", \"headers\":{}, \"queryParameters\":{}, \"pathParameters\":{}, \"body\":\"{ \\\"EventType\\\" : \\\"check_url\\\" }\", \"isBase64Encoded\":true }")
            ) {
                requestJson ->
                val request = objectMapper.readValue<APIRequestKt>(requestJson, APIRequestKt::class.java)
                request.isBase64Encoded shouldBe true
                request.path shouldBe "api request path"
                request.body shouldNotBe null
            }
        }

        "deserialize from inpustream" {
            val stream = ByteArrayInputStream("{ \"path\":\"api request path\", \"httpMethod\":\"request method name\", \"headers\":{\"content-type\":\"application/json\"}, \"queryParameters\":{}, \"pathParameters\":{}, \"body\":\"xxx\", \"isBase64Encoded\":false }".toByteArray())
            val request = objectMapper.readValue<APIRequestKt>(stream, APIRequestKt::class.java)
                request.isBase64Encoded shouldBe false
                request.path shouldBe "api request path"
                request.body shouldBe "xxx"
        }
    }
}
