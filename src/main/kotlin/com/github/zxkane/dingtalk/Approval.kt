package com.github.zxkane.dingtalk

import com.aliyun.fc.runtime.Context
import com.aliyun.fc.runtime.StreamRequestHandler
import com.google.gson.Gson
import java.io.InputStream
import java.io.OutputStream

open class APIRequest {
    lateinit var path: String
    lateinit var httpMethod: String
    lateinit var headers: Map<String, String>
    lateinit var queryParameters: Map<String, String>
    lateinit var pathParameters: Map<String, String>
    lateinit var body: String
    var isBase64Encoded: Boolean = false
}

data class APIResponse(
    val body: String,
    val headers: Map<String, String>,
    val isBase64Encoded: Boolean,
    val statusCode: Int
)

class Approval : StreamRequestHandler {

    override fun handleRequest(input: InputStream, output: OutputStream, context: Context) {
        context.logger.debug("${context.functionParam.functionName}/${context.functionParam.functionHandler}")
        val response = APIResponse("hello!", mapOf("content-type" to "text/plain"), false, 200)
        val outputJsonStr = Gson().toJson(response)
        output.write(outputJsonStr.toByteArray())
    }
}