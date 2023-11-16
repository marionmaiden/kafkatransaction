package com.example.KafkaTransactionSpring2

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.RuntimeException

@Service
class Processor {

    @Autowired
    lateinit var streamBridge: StreamBridge

    @Autowired
    lateinit var configuration: Configuration

    @Transactional
    fun process(s:String) {

        System.err.println("PROCESSING ${s}")
        val sent = streamBridge.send("eventListener-out-0", s.uppercase())
        System.err.println("SENT ${sent}")

        when {
            configuration.isDelay() -> {
                System.err.println("TIMING OUT 21s")
                Thread.sleep(21000)
            }
            configuration.isException() -> {
                System.err.println("SIMULATING EXCEPTION")
                throw RuntimeException("Simulated exception")
            }
        }


    }

}