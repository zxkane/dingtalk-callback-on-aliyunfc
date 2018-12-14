package com.github.zxkane.dingtalk

import com.aliyun.fc.runtime.Context
import com.aliyun.fc.runtime.StreamRequestHandler
import java.io.InputStream
import java.io.OutputStream

class Approval : StreamRequestHandler {

    override fun handleRequest(input: InputStream?, output: OutputStream, context: Context?) {
        output.write(java.lang.String("hello world").getBytes());
    }

}