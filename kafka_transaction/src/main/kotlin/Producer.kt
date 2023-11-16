import org.apache.commons.lang3.RandomStringUtils
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.errors.ProducerFencedException
import java.util.*

private const val OUTPUT_TOPIC = "output"

class Producer {
    private var producer: KafkaProducer<String, String> = createKafkaProducer()

    private fun createKafkaProducer(): KafkaProducer<String, String> {
        val props = Properties()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9099"
        props[ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG] = "true"
        props[ProducerConfig.TRANSACTION_TIMEOUT_CONFIG] = 5000
        props[ProducerConfig.TRANSACTIONAL_ID_CONFIG] = "prod-${RandomStringUtils.randomAlphanumeric(3)}-"
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = "org.apache.kafka.common.serialization.StringSerializer"
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = "org.apache.kafka.common.serialization.StringSerializer"
        val producerOut:KafkaProducer<String,String> = KafkaProducer(props)
        producerOut.initTransactions()
        return producerOut
    }


    fun produceInTransaction(message:String, delay:Boolean, exception:Boolean) {
        try {
            producer.beginTransaction()
            println(".Transaction Began")

            producer.send(ProducerRecord<String, String>(OUTPUT_TOPIC, "1", message))

            println("PRODUCING ${message} in transaction")

            when {
                delay -> {
                    System.err.println("TIMING OUT 10s")
                    Thread.sleep(10000)
                }
                exception -> {
                    System.err.println("SIMULATING EXCEPTION")
                    throw RuntimeException("Simulated exception")
                }
            }

            println(".Transaction Commit begin")
            producer.commitTransaction()
            println(".Transaction Commit end")
        }
        catch (e: ProducerFencedException) {
            resetProducer()
            println("ProducerFencedException in transaction: ${e}")
        }
        catch (e:RuntimeException) {
            producer.abortTransaction()
            println("Exception in transaction: ${e}")
        }
    }

    private fun resetProducer() {
        producer = createKafkaProducer()
    }
}