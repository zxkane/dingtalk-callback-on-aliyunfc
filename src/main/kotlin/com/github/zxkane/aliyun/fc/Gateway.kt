package com.github.zxkane.aliyun.fc

import com.fasterxml.jackson.annotation.JsonProperty

data class APIRequestKt(
    @JsonProperty("path", required = true) val path: String,
    @JsonProperty("httpMethod", required = true) val httpMethod: String,
    @JsonProperty("headers", required = true) val headers: Map<String, Any>,
    @JsonProperty("queryParameters", required = true) val queryParameters: Map<String, Any>,
    @JsonProperty("pathParameters", required = true) val pathParameters: Map<String, Any>,
    @JsonProperty("body", required = true) val body: String,
    @JsonProperty("isBase64Encoded", required = true) val isBase64Encoded: Boolean
)

data class APIResponse(
    val body: String,
    val headers: Map<String, Any>,
    private val isBase64Encoded: Boolean,
    val statusCode: Int
) {
    fun getIsBase64Encoded(): Boolean = isBase64Encoded
}
