package com.example.KafkaTransactionSpring

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.transaction.annotation.EnableTransactionManagement

@SpringBootApplication
class KafkaTransactionSpringApplication

fun main(args: Array<String>) {
	runApplication<KafkaTransactionSpringApplication>(*args)
}
