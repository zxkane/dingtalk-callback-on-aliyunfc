package com.github.zxkane.aliyun.fc

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class APIRequest(
    @JsonProperty("path", required = true) val path: String,
    @JsonProperty("httpMethod", required = true) val httpMethod: String,
    @JsonProperty("headers", required = true) val headers: Map<String, String>,
    @JsonProperty("queryParameters", required = true) val queryParameters: Map<String, String>,
    @JsonProperty("pathParameters", required = true) val pathParameters: Map<String, String>,
    @JsonProperty("body", required = true) val body: String,
    @JsonProperty("isBase64Encoded", required = true) val isIsBase64Encoded: Boolean
)

data class APIResponse(
    val body: String,
    val headers: Map<String, Any>,
    private val isBase64Encoded: Boolean,
    val statusCode: Int
) {
    fun getIsBase64Encoded(): Boolean = isBase64Encoded
}
