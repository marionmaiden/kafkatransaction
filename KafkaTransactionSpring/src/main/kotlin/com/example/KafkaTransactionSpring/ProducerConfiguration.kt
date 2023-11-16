package com.example.KafkaTransactionSpring

import org.apache.commons.lang3.RandomStringUtils
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.transaction.KafkaTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement


@Configuration
@EnableTransactionManagement
class ProducerConfiguration {

    @Bean
    fun kafkaTransactionManager(producerFactory: ProducerFactory<String, String>?): KafkaTransactionManager<String, String> {
        return KafkaTransactionManager(producerFactory)
    }

    @Bean
    fun producerFactory(): ProducerFactory<String, String> {
        val props: MutableMap<String, Any> = HashMap()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9099"
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.TRANSACTION_TIMEOUT_CONFIG] = 5000
        props[ProducerConfig.TRANSACTIONAL_ID_CONFIG] = "prod-${RandomStringUtils.randomAlphanumeric(3)}-"
        return DefaultKafkaProducerFactory<String, String>(props)
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, String> {
        val kafkaTemplate: KafkaTemplate<String, String> = KafkaTemplate(producerFactory())
        return kafkaTemplate
    }
}