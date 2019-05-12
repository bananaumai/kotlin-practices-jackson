package dev.bananaumai.practices.jackson

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

val reloadSync = """
    {"action":"reload","rev":47,"numItems":4,"items":[{"key":"currentSession","value":{"beginTime":"2019-04-17T06:58:08.206Z","deviceId":33747,"homeId":1548,"schemaMap":{"ACCELEROMETER":3,"OBDII":5,"PEMOTION":4,"SEAT_SENSOR_WAVE":2,"VEHICLE_LOCATION":1,"VIDEO":6},"sessionId":40,"timestamp":"2019-04-17T15:53:22.369+09:00"},"modificationTime":"2019-04-17T08:44:18.729Z"},{"key":"desiredCurrentSession","value":{"beginTime":"2019-05-09T23:57:42.984Z","deviceId":33747,"homeId":1548,"schemaMap":{"AccelerometerSchema":2,"VEHICLE_LOCATION":1},"sessionId":113,"timestamp":"2019-05-09T23:57:41.788Z"},"modificationTime":"2019-05-09T23:57:42.981Z"},{"key":"firmwareVersion","value":{"current":""},"modificationTime":"2019-04-17T08:44:18.883Z"},{"key":"lastLocation","value":{"latitude":37.326555,"longitude":-122.039915},"modificationTime":"2019-04-17T08:51:14.785Z"}]}
""".trimIndent()

val deleteSync = """
    {"action":"delete","rev":48,"key":"desiredCurrentSession"}
""".trimIndent()

val setSync = """
    {"action":"set","rev":49,"key":"desiredCurrentSession","value":{"beginTime":"2019-05-10T18:58:03.595Z","deviceId":33747,"homeId":1548,"schemaMap":{"AccelerometerSchema":2,"VEHICLE_LOCATION":1},"sessionId":121,"timestamp":"2019-05-10T18:58:02.345Z"}}
""".trimIndent()

val objectMapper = jacksonObjectMapper().apply {
    //findAndRegisterModules()
    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
}

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class Event(
    val name: String,
    val body: Any,
    @JsonSerialize(using = ZonedDateTimeSerializer::class)
    @JsonDeserialize(using = ZonedDateTimeDeserializer::class)
    val createdAt: ZonedDateTime?
)

val formatter:DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneOffset.UTC)

class ZonedDateTimeSerializer : JsonSerializer<ZonedDateTime>() {
    override fun serialize(value: ZonedDateTime, gen: JsonGenerator, arg2: SerializerProvider) {
        gen.writeString(formatter.format(value))
    }
}

class ZonedDateTimeDeserializer : JsonDeserializer<ZonedDateTime?>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): ZonedDateTime? {
        if (p == null) return null
        return ZonedDateTime.parse(p.text, formatter)
    }
}

fun main() {
    for (x in listOf(reloadSync, deleteSync, setSync)) {
        val decoded = objectMapper.readValue<Map<String, Any>>(x)

        if (decoded["hoge"] == "hoge") {
            println("oops!?")
        } else {
            println("yes")
        }

        val action = decoded["action"]
        println("action: $action (${action!!::class.java})")

        when(action) {
            "reload" -> {
                val rev = decoded["rev"]
                println("   rev is $rev (${rev!!::class.java})")

                val items = decoded["items"]
                println("   items is $items (${items!!::class.java.name})")

                for (item in decoded["items"] as List<Any>) {
                    println("        item is ${item} (${item::class.java})")
                }
            }
        }
    }

    println(objectMapper.writeValueAsString(
        Event("test", listOf(1, 2, 3), ZonedDateTime.now(ZoneId.of("UTC")))
    ))

    println(objectMapper.writeValueAsString(
        Event("test", listOf(1, 2, 3), null)
    ))

    val m: Map<String, Any> = mapOf("foo" to "foo", "time" to ZonedDateTime.now(ZoneId.of("UTC")))
    // time will not be serialized in proper way.
    println(objectMapper.writeValueAsString(m))

    val test = """
        {"name":"test","body":[1,2,3],"createdAt":"2019-05-11T00:30:35.645660Z"}
    """.trimIndent()
    println(objectMapper.readValue<Event>(test))

    val test2 = """
        {"name":"test","body":[1,2,3],"createdAt":"2019-05-11T00:30:35.645Z"}
    """.trimIndent()
    println(objectMapper.readValue<Event>(test2))

    val test3 = """
        {"name":"test","body":[1,2,3],"createdAt":"2019-05-11T00:30:35Z"}
    """.trimIndent()
    println(objectMapper.readValue<Event>(test3))

    val test4 = """
        {"name":"test","body":[1,2,3],"createdAt":"2019-05-11T00:30:35.645660Z[UTC]"}
    """.trimIndent()
    println(objectMapper.readValue<Event>(test4))

}