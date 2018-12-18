
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.zxkane.dingtalk.BPMEvent
import com.github.zxkane.dingtalk.EncryptedEvent
import io.kotlintest.data.forall
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.StringSpec
import io.kotlintest.tables.row

class DingTalkModelTests : StringSpec() {

    init {
        val objectMapper = ObjectMapper()
        objectMapper.registerModule(JavaTimeModule()).registerKotlinModule()
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        "can deserialize check url event" {
            val eventJson = "{\n" +
                    "    \"EventType\" : \"check_url\"\n" +
                    "}\n"
            val event = objectMapper.readValue<BPMEvent>(eventJson, BPMEvent::class.java)
            event.eventType shouldBe "check_url"
            event.staff shouldBe null
        }

        "can deserialize kinds of bpm events" {
            forall(
                row("{\n" +
                        "    \"EventType\": \"bpms_instance_change\",\n" +
                        "    \"processInstanceId\": \"ad253df6-e175caf-68085c60ba8a\",\n" +
                        "    \"corpId\": \"ding2c4d8175651\",\n" +
                        "    \"createTime\": 1495592259000,\n" +
                        "    \"bizCategoryId\": \"bizCategoryId\",\n" +
                        "    \"title\": \"自测-1016\",\n" +
                        "    \"type\": \"start\",\n" +
                        "    \"staffId\": \"er5875\",\n" +
                        "    \"url\": \"https://aflow.dingtalk.com/dingtalk/mobile/homepage.htm\"\n" +
                        "  }"),
                row("{\n" +
                        "    \"EventType\": \"bpms_instance_change\",\n" +
                        "    \"processInstanceId\": \"ad253df6-e175caf-68085c60ba8a\",\n" +
                        "    \"finishTime\": 1495592305000,\n" +
                        "    \"corpId\": \"ding2c015874d8175651\",\n" +
                        "    \"title\": \"自测-1016\",\n" +
                        "    \"type\": \"finish\",\n" +
                        "    \"url\": \"https://aflow.dingtalk.com/dingtalk/mobile/homepage.htm?corpid=ding2c015874d8175651&dd_share=\",\n" +
                        "    \"result\": \"refuse\",\n" +
                        "    \"createTime\": 1495592272000,\n" +
                        "    \"bizCategoryId\": \"bizCategoryId\",\n" +
                        "    \"staffId\": \"manager75\"\n" +
                        "  }"),
                row("{\n" +
                        "    \"EventType\": \"bpms_task_change\",\n" +
                        "    \"processInstanceId\": \"ce133dd0-5b22-9516-925779977e9c\",\n" +
                        "    \"corpId\": \"ding2c015874d8175651\",\n" +
                        "    \"createTime\": 1495593189000,\n" +
                        "    \"bizCategoryId\": \"bizCategoryId\",\n" +
                        "    \"title\": \"自测-1016\",\n" +
                        "    \"type\": \"start\",\n" +
                        "    \"staffId\": \"manager75\"\n" +
                        "  }"),
                row("{\n" +
                        "    \"EventType\": \"bpms_task_change\",\n" +
                        "    \"processInstanceId\": \"ce133dd0-5b22-9516-925779977e9c\",\n" +
                        "    \"corpId\": \"ding2c015874d8175651\",\n" +
                        "    \"createTime\": 1495593189000,\n" +
                        "    \"bizCategoryId\": \"bizCategoryId\",\n" +
                        "    \"title\": \"自测-1016\",\n" +
                        "    \"type\": \"start\",\n" +
                        "    \"staffId\": \"manager75\"\n" +
                        "  }"),
                row("{\n" +
                        "    \"EventType\": \"bpms_task_change\",\n" +
                        "    \"processInstanceId\": \"439bda1c-d9-9d67-8081ede79716\",\n" +
                        "    \"finishTime\": 1495542282000,\n" +
                        "    \"corpId\": \"ding2c015874d8175651\",\n" +
                        "    \"title\": \"自测-2017\",\n" +
                        "    \"type\": \"finish\",\n" +
                        "    \"result\": \"redirect\",\n" +
                        "    \"createTime\": 1495541847000,\n" +
                        "    \"bizCategoryId\": \"bizCategoryId\",\n" +
                        "    \"staffId\": \"08058646137\"\n" +
                        "  }")
            ) {
                eventJson ->
                    val event = objectMapper.readValue<BPMEvent>(eventJson, BPMEvent::class.java)
                    event.eventType shouldContain "bpms_"
                    event.instanceId shouldNotBe  null
            }
        }

        "deserialize encrypted event" {
            forall(
                    row("{\"encrypt\":\"kKqdrHvhsH2xFw8qFJpuSo3DH8+7/hZIOEJZLsE9dfIHBy+HzF54lnuMP3B5r9GbCPXA/r2rCDnwpyusGMYpKxafcMdzwwySOWqkGzbJ6yOjVnfWHq5zCqBrEVL8eZV9\"}")
            ) {
                json ->
                val event = objectMapper.readValue<EncryptedEvent>(json, EncryptedEvent::class.java)
                event.encrypt shouldNotBe null
            }
        }
    }
}
