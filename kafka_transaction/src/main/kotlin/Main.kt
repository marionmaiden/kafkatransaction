import org.apache.commons.lang3.RandomStringUtils
import java.util.concurrent.CompletableFuture


val producer = Producer()

fun main() {
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

    producer.produceInTransaction("test_${RandomStringUtils.randomAlphanumeric(1)}", false, true)

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
        producer.produceInTransaction("test_${RandomStringUtils.randomAlphanumeric(1)}", false, true)
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

    producer.produceInTransaction("test_${RandomStringUtils.randomAlphanumeric(1)}", true, false)

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

    future.join()

    assert(consumer.readDirty().isEmpty)
    assert(consumer.readClean().isEmpty)
}