package com.example.KafkaTransactionSpring

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.RuntimeException

@Service
class ProducerService (var producer: Producer) {

    @Transactional
    fun produceInTransaction(message:String, delay:Boolean, exception:Boolean){
        try {
            println("PRODUCING ${message} in transaction")
            producer.produce(message)

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
        }
        catch (e:RuntimeException) {
            System.err.println("Exception caught in Service class: ${e}")
            throw e
        }
    }

}