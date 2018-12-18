package com.github.zxkane.dingtalk

import com.aliyun.fc.runtime.Context
import com.aliyun.fc.runtime.PojoRequestHandler
import com.github.zxkane.aliyun.fc.APIRequest
import com.github.zxkane.aliyun.fc.APIResponse

class Approval : PojoRequestHandler<APIRequest, APIResponse> {
    override fun handleRequest(input: APIRequest?, context: Context?): APIResponse {
        val response = APIResponse("hello!", mapOf("content-type" to "text/plain"),
            false, 200)
        return response
    }
}