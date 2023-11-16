package com.example.KafkaTransactionSpring2

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.kafka.core.KafkaAdmin
import java.util.*
import java.util.concurrent.CompletableFuture

const val BOOTSTRAP_SERVERS = "localhost:9099"

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation::class)
class KafkaTransactionSpring2ApplicationTests {

	@MockBean
	lateinit var configuration: Configuration

	@Autowired
	lateinit var admin: KafkaAdmin
	lateinit var client: AdminClient

	var producer: KafkaProducer<String, String>? = null

	private fun createKafkaProducer(): KafkaProducer<String, String> {
		if (producer == null) {
			val props = Properties()
			props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = BOOTSTRAP_SERVERS
			props[ProducerConfig.CLIENT_ID_CONFIG] = "myTestProducer"
			props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = "org.apache.kafka.common.serialization.StringSerializer"
			props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] =
				"org.apache.kafka.common.serialization.StringSerializer"
			producer = KafkaProducer(props)
		}
		return producer as KafkaProducer<String, String>
	}

	@BeforeAll
	fun setup() {
		val topicNames:List<String> = listOf(
			"inputTopic", "outputTopic"
		)

		val consumerGroups:List<String> = listOf(
			"group-CLEAN-read-1",
			"group-CLEAN-read-2",
			"group-CLEAN-read-3",
			"group-CLEAN-read-4",
			"group-CLEAN-read-5",
			"group-DIRTY-read-1",
			"group-DIRTY-read-2",
			"group-DIRTY-read-3",
			"group-DIRTY-read-4",
			"group-DIRTY-read-5",
		)

		val configs = HashMap<String, Any>()
		configs.put("bootstrap.servers", BOOTSTRAP_SERVERS)

		client = AdminClient.create(configs)
		client.deleteTopics(topicNames)
		client.deleteConsumerGroups(consumerGroups)
	}

//	@Disabled
	@Order(1)
	@Test
	fun happyPath() {
		System.err.println("************************************")
		System.err.println("Happy Path")
		System.err.println("************************************")

		val message = "first_message"

		val producer = createKafkaProducer()
		val consumer = Consumer(1)

		Mockito.`when`(configuration.isDelay()).thenReturn(false)
		Mockito.`when`(configuration.isException()).thenReturn(false)

		assertThat(consumer.readDirty()).isEmpty()
		assertThat(consumer.readClean()).isEmpty()

		producer.send(ProducerRecord("inputTopic", "1", message))

		val records = consumer.readClean()
		assertThat(consumer.readDirty().count()).isEqualTo(1)

		assertThat(records.count()).isEqualTo(1)
		assertThat(records.first().value()).isEqualTo(message.uppercase())
	}

//	@Disabled
	@Order(2)
	@Test
	fun testTransactionalConsumerException() {
		System.err.println("************************************")
		System.err.println("Exception case")
		System.err.println("************************************")

		val producer = createKafkaProducer()
		val consumer = Consumer(2)

		val message = "second_message"

		Mockito.`when`(configuration.isDelay()).thenReturn(false)
		Mockito.`when`(configuration.isException()).thenReturn(true)

		assertThat(consumer.readDirty()).isEmpty()
		assertThat(consumer.readClean()).isEmpty()

		producer.send(ProducerRecord("inputTopic", "1", message))

		// This is very hard to reproduce. Sometimes we have the dirty message, sometimes not!!
		consumer.readDirty()
		assertThat(consumer.readClean()).isEmpty()
	}

//	@Disabled
	@Order(3)
	@Test
	fun happyPath2() {
		System.err.println("************************************")
		System.err.println("Happy Path 2")
		System.err.println("************************************")

		val message = "third_message"

		val producer = createKafkaProducer()
		val consumer = Consumer(3)

		Mockito.`when`(configuration.isDelay()).thenReturn(false)
		Mockito.`when`(configuration.isException()).thenReturn(false)

		assertThat(consumer.readDirty()).isEmpty()
		assertThat(consumer.readClean()).isEmpty()

		producer.send(ProducerRecord("inputTopic", "1", message))

		val records = consumer.readClean()
		// Dirt read will read the uncommitted message
		assertThat(consumer.readDirty().count()).isEqualTo(1)

		assertThat(records.count()).isEqualTo(1)
		assertThat(records.first().value()).isEqualTo(message.uppercase())
	}

//	@Disabled
	@Order(4)
	@Test
	fun testTransactionalConsumerTimeout() {
		System.err.println("************************************")
		System.err.println("Timeout case")
		System.err.println("************************************")

		val producer = createKafkaProducer()
		val consumer = Consumer(4)

		val message = "fourth_message"

		Mockito.`when`(configuration.isDelay()).thenReturn(true)
		Mockito.`when`(configuration.isException()).thenReturn(false)

		assertThat(consumer.readDirty()).isEmpty()
		assertThat(consumer.readClean()).isEmpty()

		val future = CompletableFuture.runAsync {
			producer.send(ProducerRecord("inputTopic", "1", message))
		}

		// Dirt read will read the uncommitted message
		val records = consumer.readDirty()

		assertThat(records.count()).isEqualTo(1)
		assertThat(records.first().value()).isEqualTo(message.uppercase())
		assertThat(consumer.readClean()).isEmpty()

		future.join()

		assertThat(consumer.readClean()).isEmpty()
		assertThat(consumer.readDirty()).isEmpty()
	}

//	@Disabled
	@Order(5)
	@Test
	fun happyPath3() {
		System.err.println("************************************")
		System.err.println("Happy Path 3")
		System.err.println("************************************")

		val message = "fifth_message"

		val producer = createKafkaProducer()
		val consumer = Consumer(5)

		Mockito.`when`(configuration.isDelay()).thenReturn(false)
		Mockito.`when`(configuration.isException()).thenReturn(false)

		assertThat(consumer.readClean()).isEmpty()
		assertThat(consumer.readDirty()).isEmpty()

		producer.send(ProducerRecord("inputTopic", "1", message))

		val records = consumer.readClean()
		assertThat(consumer.readDirty().count()).isEqualTo(1)

		assertThat(records.count()).isEqualTo(1)
		assertThat(records.first().value()).isEqualTo(message.uppercase())
	}

}
