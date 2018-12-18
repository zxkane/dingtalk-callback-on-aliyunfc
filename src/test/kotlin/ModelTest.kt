import com.fasterxml.jackson.databind.ObjectMapper
import com.github.zxkane.aliyun.fc.APIRequest
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class ModelTests : StringSpec() {

    init {
        val objectMapper = ObjectMapper()

        "can deserialize API Request" {
            val requestJson = "{ \"path\":\"api request path\", \"httpMethod\":\"request method name\", \"headers\":{}, \"queryParameters\":{}, \"pathParameters\":{}, \"body\":\"string of request payload\", \"isBase64Encoded\":true }"
            val request = objectMapper.readValue<APIRequest>(requestJson, APIRequest::class.java)
            request.isIsBase64Encoded shouldBe true
            request.path shouldBe "api request path"
        }
    }
}
