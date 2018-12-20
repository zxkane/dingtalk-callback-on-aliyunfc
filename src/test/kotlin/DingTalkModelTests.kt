
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.zxkane.dingtalk.EncryptedEvent
import com.github.zxkane.dingtalk.Event
import io.kotlintest.data.forall
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.StringSpec
import io.kotlintest.tables.row

class DingTalkModelTests : StringSpec() {

    init {
        val objectMapper = ObjectMapper().registerModule(JavaTimeModule()).registerKotlinModule()
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        "can deserialize check url event" {
            val eventJson = "{\n" +
                    "    \"EventType\" : \"check_url\"\n" +
                    "}\n"
            val event = objectMapper.readValue<Event>(eventJson, Event::class.java)
            event.shouldBeTypeOf<Event.CheckEvent>()
            event.type shouldBe "check_url"
        }

        "can deserialize kinds of org events"{
            forall(
                row("""
                    {
                        "EventType": "user_add_org",
                        "TimeStamp": 43535463645,
                        "UserId": ["efefef" , "111111"],
                        "CorpId": "corpid"
                    }
                """.trimIndent())
            ){
                    eventJson ->
                        val event = objectMapper.readValue<Event>(eventJson, Event::class.java)
                        event.shouldBeTypeOf<Event.OrgEvent>()
                        val orgEvent = event as Event.OrgEvent
                        orgEvent.eventType shouldContain "org"
                        orgEvent.timeStamp shouldNotBe null
            }
        }

        "can deserialize kinds of bpm events" {
            forall(
                row("""{
                            "EventType": "bpms_instance_change",
                            "processInstanceId": "ad253df6-e175caf-68085c60ba8a",
                            "corpId": "ding2c4d8175651",
                            "createTime": 1495592259000,
                            "bizCategoryId": "bizCategoryId",
                            "title": "自测-1016",
                            "type": "start",
                            "staffId": "er5875",
                            "url": "https://aflow.dingtalk.com/dingtalk/mobile/homepage.htm"
                          }"""),
                row("""
                    {
                        "EventType": "bpms_instance_change",
                        "processInstanceId": "ad253df6-e175caf-68085c60ba8a",
                        "finishTime": 1495592305000,
                        "corpId": "ding2c015874d8175651",
                        "title": "自测-1016",
                        "type": "finish",
                        "url": "https://aflow.dingtalk.com/dingtalk/mobile/homepage.htm?corpid=ding2c015874d8175651&dd_share=",
                        "result": "refuse",
                        "createTime": 1495592272000,
                        "bizCategoryId": "bizCategoryId",
                        "staffId": "manager75"
                      }
                """.trimIndent()),
                row("""
                    {
                        "EventType": "bpms_task_change",
                        "processInstanceId": "ce133dd0-5b22-9516-925779977e9c",
                        "corpId": "ding2c015874d8175651",
                        "createTime": 1495593189000,
                        "bizCategoryId": "bizCategoryId",
                        "title": "自测-1016",
                        "type": "start",
                        "staffId": "manager75"
                      }
                """.trimIndent()),
                row("""
                    {
                        "EventType": "bpms_task_change",
                        "processInstanceId": "ce133dd0-5b22-9516-925779977e9c",
                        "finishTime": 1495605749000,
                        "corpId": "ding2c01651",
                        "title": "自测-1016",
                        "type": "finish",
                        "result": "refuse",
                        "remark": "拒绝理由",
                        "createTime": 1495593189000,
                        "bizCategoryId": "bizCategoryId",
                        "staffId": "manager75"
                      }
                """.trimIndent()),
                row("""
                    {
                        "EventType": "bpms_task_change",
                        "processInstanceId": "439bda1c-d9-9d67-8081ede79716",
                        "finishTime": 1495542282000,
                        "corpId": "ding2c015874d8175651",
                        "title": "自测-2017",
                        "type": "finish",
                        "result": "redirect",
                        "createTime": 1495541847000,
                        "bizCategoryId": "bizCategoryId",
                        "staffId": "08058646137"
                      }
                """.trimIndent())
            ) {
                eventJson ->
                    val event = objectMapper.readValue<Event>(eventJson, Event::class.java)
                    event.shouldBeTypeOf<Event.BPMEvent>()
                    val bpmEvent = event as Event.BPMEvent
                    bpmEvent.type shouldContain "bpms_"
                    bpmEvent.processInstanceId shouldNotBe  null
                    bpmEvent.bpmType shouldNotBe null
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
