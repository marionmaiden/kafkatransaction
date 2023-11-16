package com.example.KafkaTransactionSpring2

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service

@Service
class Listener {

    @Autowired
    lateinit var processor: Processor

    @Bean
    open fun eventListener(): (String) -> Unit {
        return  {
//            System.err.println("LISTENING ${it}")
            processor.process(it)
        }
    }


}