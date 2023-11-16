package com.example.KafkaTransactionSpring2

import org.springframework.stereotype.Component

@Component
class Configuration {


    fun isDelay(): Boolean {
        return false
    }


    fun isException(): Boolean {
        return false
    }


}