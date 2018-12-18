package com.github.zxkane.aliyun.fc;

import java.util.Map;

public class APIRequest {
    private String path;
    private String httpMethod;
    private Map<String, String> headers;
    private Map<String, String> queryParameters;
    private Map<String, String> pathParameters;
    private String body;
    private boolean isBase64Encoded;

    @Override
    public String toString() {
        return "Request{" +
                "path='" + path + '\'' +
                ", httpMethod='" + httpMethod + '\'' +
                ", headers=" + headers +
                ", queryParameters=" + queryParameters +
                ", pathParameters=" + pathParameters +
                ", body='" + body + '\'' +
                ", isBase64Encoded=" + isBase64Encoded +
                '}';
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, String> getQueryParameters() {
        return queryParameters;
    }

    public void setQueryParameters(Map<String, String> queryParameters) {
        this.queryParameters = queryParameters;
    }

    public Map<String, String> getPathParameters() {
        return pathParameters;
    }

    public void setPathParameters(Map<String, String> pathParameters) {
        this.pathParameters = pathParameters;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean getIsBase64Encoded() {
        return this.isBase64Encoded;
    }

    public void setIsBase64Encoded(boolean base64Encoded) {
        this.isBase64Encoded = base64Encoded;
    }
}
