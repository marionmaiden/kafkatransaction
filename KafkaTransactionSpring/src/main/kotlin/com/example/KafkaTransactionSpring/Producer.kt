package com.example.KafkaTransactionSpring

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

private const val INPUT_TOPIC_NAME = "output2"

@Component
class Producer(var kafkaTemplate: KafkaTemplate<String, String>) {

    @Transactional
    fun produce(message:String) {
        println("Produced message: $message")
        kafkaTemplate!!.send(INPUT_TOPIC_NAME, "1", message)
    }

}