import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.cloudevents.CloudEvent
import io.cloudevents.core.builder.CloudEventBuilder
import io.cloudevents.core.message.Encoding
import io.cloudevents.core.provider.EventFormatProvider
import io.cloudevents.jackson.JsonFormat
import io.cloudevents.kafka.CloudEventDeserializer
import io.cloudevents.kafka.CloudEventSerializer
import org.apache.kafka.common.header.internals.RecordHeaders
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.OffsetDateTime
import java.util.*

data class AddressChanged(
    val id: String,
    val baseAddress: String,
    val detailAddress: String,
    val zipCode: String,
)

class CloudEventsTests {
    val eventFormat = EventFormatProvider.getInstance()
        // force base64
//        .also {
//            it.registerFormat(JsonFormat().withForceJsonDataToBase64())
//        }
        .resolveFormat(JsonFormat.CONTENT_TYPE)!!
    val objectMapper = ObjectMapper()
        .registerModule(KotlinModule())

    @Test
    fun jacksonSerDeser() {
        val event = AddressChanged(
            id = UUID.randomUUID().toString(),
            baseAddress = UUID.randomUUID().toString(),
            detailAddress = UUID.randomUUID().toString(),
            zipCode = UUID.randomUUID().toString(),
        )

        val cloudEvent = CloudEventBuilder.v1()
            .withId(UUID.randomUUID().toString())
            .withType(event.javaClass.name)
            .withSource(URI.create("/people/${event.id}"))
            .withSubject("address")
            .withTime(OffsetDateTime.now())
            .withDataContentType("application/json")
            .withDataSchema(null)
            .withData { objectMapper.writeValueAsBytes(event) }
            .withExtension("astring", "aaa")
            .withExtension("aboolean", "true")
            .withExtension("anumber", "10")
            .build()

        val serialized: ByteArray = this.eventFormat.serialize(cloudEvent)

        println(this.objectMapper.readValue(serialized, JsonNode::class.java).toPrettyString())

        val deserialized: CloudEvent = this.eventFormat.deserialize(serialized)
        println("type : ${deserialized.type}")

        val deserializedEvent = objectMapper.readValue(deserialized.data!!.toBytes(), Class.forName(deserialized.type))
        println("deserializedEvent: $deserializedEvent")
        println("===========================")
    }

    val configs = mapOf(
        CloudEventSerializer.ENCODING_CONFIG to Encoding.STRUCTURED,
        CloudEventSerializer.EVENT_FORMAT_CONFIG to JsonFormat()
    )
    val kafkaSerializer = CloudEventSerializer()
        .also { it.configure(configs, false) }
    val kafkaDeserlializer = CloudEventDeserializer()

    @Test
    fun kafkaSerDeser() {
        val event = AddressChanged(
            id = UUID.randomUUID().toString(),
            baseAddress = UUID.randomUUID().toString(),
            detailAddress = UUID.randomUUID().toString(),
            zipCode = UUID.randomUUID().toString(),
        )

        val cloudEvent = CloudEventBuilder.v1()
            .withId(UUID.randomUUID().toString())
            .withType(event.javaClass.name)
            .withSource(URI.create("/people/${event.id}"))
            .withSubject("address")
            .withTime(OffsetDateTime.now())
            .withDataContentType("application/json")
            .withDataSchema(null)
            .withData { objectMapper.writeValueAsBytes(event) }
            .withExtension("astring", "aaa")
            .withExtension("aboolean", "true")
            .withExtension("anumber", "10")
            .build()

        val headers = RecordHeaders()
        val serialized: ByteArray = kafkaSerializer.serialize("topic", headers, cloudEvent)
        println(this.objectMapper.readValue(serialized, JsonNode::class.java).toPrettyString())
        println(headers)

        val deserialized: CloudEvent = kafkaDeserlializer.deserialize("topic", headers, serialized)
        println("type : ${deserialized.type}")

        val deserializedEvent = objectMapper.readValue(deserialized.data!!.toBytes(), Class.forName(deserialized.type))
        println("deserializedEvent: $deserializedEvent")
        println("===========================")
    }
}
