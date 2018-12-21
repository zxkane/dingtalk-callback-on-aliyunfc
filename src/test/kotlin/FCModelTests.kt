
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.zxkane.aliyun.fc.APIRequest
import com.github.zxkane.aliyun.fc.APIResponse
import com.github.zxkane.dingtalk.STATUS_CODE
import io.kotlintest.data.forall
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.StringSpec
import io.kotlintest.tables.row
import java.io.ByteArrayInputStream

class FCModelTests : StringSpec() {

    init {
        val objectMapper = ObjectMapper()

        "can deserialize API Request" {
            forall(
                row("""
                    {
                        "path":"api request path",
                        "httpMethod":"request method name",
                        "headers":{},
                        "queryParameters":{},
                        "pathParameters":{},
                        "body":"string of request payload",
                        "isBase64Encoded":true
                    }
                """.trimIndent()),
                row("""
                    {
                        "path":"api request path",
                        "httpMethod":"request method name",
                        "headers":{},
                        "queryParameters":{},
                        "pathParameters":{},
                        "body":"{ \"eventType\" : \"check_url\" }",
                        "isBase64Encoded":true
                    }
                """.trimIndent())
            ) {
                requestJson ->
                val request = objectMapper.readValue<APIRequest>(requestJson, APIRequest::class.java)
                request.isIsBase64Encoded shouldBe true
                request.path shouldBe "api request path"
                request.body shouldNotBe null
            }
        }

        "deserialize API Request from input stream" {
            val stream = ByteArrayInputStream("""
                {
                    "path":"api request path",
                    "httpMethod":"request method name",
                    "headers":{"content-eventType":"application/json"},
                    "queryParameters":{},
                    "pathParameters":{},
                    "body":"xxx",
                    "isBase64Encoded":false
                }
            """.trimIndent().toByteArray())
            val request = objectMapper.readValue<APIRequest>(stream, APIRequest::class.java)
                request.isIsBase64Encoded shouldBe false
                request.path shouldBe "api request path"
                request.body shouldBe "xxx"
        }

        "deserialize API Request from input stream 2" {
            val stream = ByteArrayInputStream("""
                {
                  "path": "/dingtalk",
                  "httpMethod": "POST",
                  "headers": {
                    "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8"
                  },
                  "queryParameters": {
                    "timestamp": 1545386428822,
                    "nonce": "0R8zxhMv",
                    "signature": "aa9c70167e6bc5cf7a2a3cd4d3f67764d63530ee"
                  },
                  "pathParameters": {},
                  "body": "eyJlbmNyeXB0IjoiMnNRZUt0c0I1WHo4ZDFEb0E2SmJhRU5DR0Y3UFVTWkZaVU9CZXZJWFBMOTNhTWc2a0tiMWw1MHFYVjkzNHNQSkFIY3N6NDdMaFFORFJDY2VzQzZ0SWhKUHRzNlVpVkVCdC8xU3Z2a056L2p1SWlSZ2JPVkY4STZ2RGE5b0pHdzAifQ==",
                  "isBase64Encoded": true
                }
            """.trimIndent().toByteArray())
            val request = objectMapper.readValue<APIRequest>(stream)
            request.path shouldBe "/dingtalk"
            request.isIsBase64Encoded shouldBe true
        }

        "serialize API gateway response" {
            val response = APIResponse("xxx", mapOf(), false, STATUS_CODE)
            val responseJson = objectMapper.writeValueAsString(response)
            responseJson shouldContain "\"isBase64Encoded\":false"
        }

        "serialize API gateway request" {
            val request = APIRequest("/callback", "POST", mapOf(), mapOf(), mapOf(),
                "abc", false)
            val requestJson = objectMapper.writeValueAsString(request)
            requestJson shouldContain "\"isBase64Encoded\":false"
            requestJson shouldContain "\"body\":\"abc\""
        }
    }
}
