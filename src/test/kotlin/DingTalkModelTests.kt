
import com.github.zxkane.dingtalk.EncryptedEvent
import com.github.zxkane.dingtalk.Event
import io.kotlintest.data.forall
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.tables.row
import java.time.ZonedDateTime


class DingTalkModelTests : AbstractTest() {

    init {
        super.init()
        val objectMapper = callback.objectMapper

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
                """.trimIndent()),
                row("""
                    {
                      "taskId": 48665689823,
                      "createTime": 1545354519000,
                      "staffId": "0917483931793546",
                      "bizCategoryId": "",
                      "EventType": "bpms_task_change",
                      "type": "start",
                      "title": "请假申请",
                      "processCode": "PROC-A6258EA3-B094-461D-8A40-F0822587065A",
                      "processInstanceId": "f15dcec3-105a-405b-9812-eeb99b827d01",
                      "corpId": "ding3690d27bb4ed8c8735c2f4657eb6378f"
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
                row("""
                    {"encrypt":
                        "kKqdrHvhsH2xFw8qFJpuSo3DH8+7/hZIOEJZLsE9dfIHBy+HzF54lnuMP3B5r9GbCPXA/r2rCDnwpyusGMYpKxafcMdzwwySOWqkGzbJ6yOjVnfWHq5zCqBrEVL8eZV9"}
                """.trimIndent())
            ) {
                json ->
                val event = objectMapper.readValue<EncryptedEvent>(json, EncryptedEvent::class.java)
                event.encrypt shouldNotBe null
            }
        }

        "deserialize timestamp of org event" {
            val eventJson = """
                {
                    "TimeStamp":"1545536053900",
                    "CorpId":"ding3690d27bb4ed8c8735c2f4657eb6378f",
                    "UserId":["194404002440050688"],
                    "EventType":"user_modify_org"
                }
            """.trimIndent()

            val orgEvent = objectMapper.readValue<Event>(eventJson, Event::class.java)
            orgEvent.shouldBeTypeOf<Event.OrgEvent>()
            (orgEvent as Event.OrgEvent).timeStamp.isEqual(ZonedDateTime.parse("2018-12-23T03:34:13.900+00:00"))
        }
    }

}
