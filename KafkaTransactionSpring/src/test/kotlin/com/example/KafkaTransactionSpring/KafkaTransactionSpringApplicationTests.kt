package com.example.KafkaTransactionSpring

import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import kotlin.RuntimeException

@SpringBootTest
class KafkaTransactionSpringApplicationTests {

	@Autowired
	lateinit var producer:ProducerService

	@Test
	fun testsuite() {
		happyPath()
		exceptionTestSync()
		happyPath()
		exceptionTestAsync()
		happyPath()
		timeoutTestSync()
		happyPath()
		timeoutTestAsync()
		happyPath()
	}

	fun happyPath() {
		System.err.println("************************************")
		System.err.println("Happy Path")
		System.err.println("************************************")

		val consumer = Consumer()


		assert(consumer.readClean().isEmpty)
		assert(consumer.readDirty().isEmpty)

		producer.produceInTransaction("test_${RandomStringUtils.randomAlphanumeric(1)}", false, false)

		assert(consumer.readClean().count() == 1)
	}

	fun exceptionTestSync() {
		System.err.println("************************************")
		System.err.println("Exception Test Sync")
		System.err.println("************************************")

		val consumer = Consumer()

		assert(consumer.readClean().isEmpty)
		assert(consumer.readDirty().isEmpty)

		assertThrows<RuntimeException> {
			producer.produceInTransaction("test_${RandomStringUtils.randomAlphanumeric(1)}", false, true)
		}

		assert(consumer.readDirty().isEmpty)
		assert(consumer.readClean().isEmpty)
	}

	fun exceptionTestAsync() {
		System.err.println("************************************")
		System.err.println("Exception Test Async")
		System.err.println("************************************")

		val consumer = Consumer()

		assert(consumer.readClean().isEmpty)
		assert(consumer.readDirty().isEmpty)

		val future = CompletableFuture.runAsync {
			Thread.sleep(5000)
			assertThrows<RuntimeException> {
				producer.produceInTransaction("test_${RandomStringUtils.randomAlphanumeric(1)}", false, true)
			}
		}

		// During the Async part, the message couldn't be dirt read
		assert(consumer.readDirty().isEmpty)
		assert(consumer.readClean().isEmpty)

		future.join()

		assert(consumer.readDirty().isEmpty)
		assert(consumer.readClean().isEmpty)
	}

	fun timeoutTestSync() {
		System.err.println("************************************")
		System.err.println("Timeout Test Sync")
		System.err.println("************************************")

		val consumer = Consumer()

		assert(consumer.readClean().isEmpty)
		assert(consumer.readDirty().isEmpty)


		try {
			producer.produceInTransaction("test_${RandomStringUtils.randomAlphanumeric(1)}", true, false)
		}
		catch (e: RuntimeException) {
			println("Error when timeout producer - reset Producer: ${e}")
		}

		assert(consumer.readDirty().count() == 1)
		assert(consumer.readClean().isEmpty)
	}

	fun timeoutTestAsync() {
		System.err.println("************************************")
		System.err.println("Timeout Test Async")
		System.err.println("************************************")

		val consumer = Consumer()

		assert(consumer.readClean().isEmpty)
		assert(consumer.readDirty().isEmpty)

		val future = CompletableFuture.runAsync {
			producer.produceInTransaction("test_${RandomStringUtils.randomAlphanumeric(1)}", true, false)
		}

		assert(consumer.readDirty().count() == 1)
		assert(consumer.readClean().isEmpty)

		try {
			future.join()
		}
		catch (e: CompletionException) {
			println("Error when joining Future: ${e}")
		}

		assert(consumer.readDirty().isEmpty)
		assert(consumer.readClean().isEmpty)
	}

}
