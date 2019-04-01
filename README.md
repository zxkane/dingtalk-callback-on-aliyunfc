## 部署于[阿里云函数计算][aliyun-fc]的钉钉回调接口

该程序实现了一个HTTP接口用于接受钉钉的各类事件回调，包括且不限于审批事件、组织事件等。同时事件消息被保存在[表格存储][table-store]中。

该程序使用JVM语言Kotlin开发，使用了如下阿里云产品，

- 函数计算
- 表格存储
- API网关
- 监控服务
- 日志服务
- 访问控制

### 部署步骤

#### 准备步骤

1. 从钉钉的[开发者平台](https://open-dev.dingtalk.com/#/index)获取你组织的 **corpid** 
2. 为回调函数创建密钥`DD_TOKEN`, `DD_AES_KEY`
3. 安装配置 [Fun CLI](https://help.aliyun.com/document_detail/64204.html)用于部署

#### 编译、测试、打包

```bash
# build the source
./gradlew build
```

#### 通过Fun工具部署

```bash
# 基于环境变量渲染函数部署配置, template.yml基于Go template, 支持任意Go Template工具处理
sigil -f template.tmpl \
      USING_VPC=true VPC_ID=$VPC_ID VPC_SWITCH_IDS=$VPC_SWITCHER_IDS SECURITY_GROUP_ID=$SECURITY_GROUP_ID \
      USING_LOG=true LOG_PROJECT=$LOG_PROJECT LOG_STORE=$LOG_STORE \
      DD_TOKEN=$DD_TOKEN DD_AES_KEY=$DD_AES_KEY DD_CORPID=$DD_CORPID \
      DTS_ENDPOINT=$DTS_ENDPOINT DTS_INSTANCE_NAME=$DTS_INSTANCE_NAME \
      > template.yml
    
# deploy the fc function, api gateway, table store
fun deploy
```

### 钉钉回调设置步骤

1. 获取到部署后应用的API网关地址
2. 使用[钉钉 API](https://open-doc.dingtalk.com/microapp/serverapi2/pwz3r5)注册或者更新回调地址

For example,

```bash
curl -X POST \
  'https://oapi.dingtalk.com/call_back/update_call_back?access_token=<your token>' \
  -H 'Content-Type: application/json' \
  -d '{
    "call_back_tag": [
        "bpms_task_change",
        "bpms_instance_change"
    ],
    "token": "<token created in prerequisites step 2>",
    "aes_key": "<aes token created in prerequisites step 2>",
    "url": "<部署后获得API网关地址>"
}' 

```

[aliyun-fc]: https://help.aliyun.com/document_detail/52895.html
[table-store]: https://help.aliyun.com/product/27278.html