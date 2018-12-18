package com.github.zxkane.aliyun.fc

open class APIRequest {
    lateinit var path: String
    lateinit var httpMethod: String
    lateinit var headers: Map<String, Any>
    lateinit var queryParameters: Map<String, Any>
    lateinit var pathParameters: Map<String, Any>
    lateinit var body: String
    var isIsBase64Encoded: Boolean = false
}

data class APIResponse(
    val body: String,
    val headers: Map<String, Any>,
    private val isBase64Encoded: Boolean,
    val statusCode: Int
) {
    fun getIsBase64Encoded(): Boolean = isBase64Encoded
}