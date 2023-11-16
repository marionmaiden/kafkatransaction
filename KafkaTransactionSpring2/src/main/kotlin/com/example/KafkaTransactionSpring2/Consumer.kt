package com.example.KafkaTransactionSpring2

import org.apache.commons.lang3.RandomStringUtils
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.util.*
import kotlin.collections.HashMap

const val INPUT_TOPIC = "outputTopic"

class Consumer(val id: Int?){

    private val consumer: KafkaConsumer<String, String> = createKafkaConsumer(false)
    private val dirtConsumer: KafkaConsumer<String, String> = createKafkaConsumer(true)

    private fun createKafkaConsumer(dirtRead:Boolean): KafkaConsumer<String, String> {
        val props:MutableMap<String, Any> = HashMap()

        val consumerId = id?: RandomStringUtils.randomAlphanumeric(3)

        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9099"
        props[ConsumerConfig.CLIENT_ID_CONFIG] = "client-${RandomStringUtils.randomAlphanumeric(5)}"
        if (dirtRead) {
            props[ConsumerConfig.GROUP_ID_CONFIG] = "group-DIRTY-read-${consumerId}"
            props[ConsumerConfig.ISOLATION_LEVEL_CONFIG] = "read_uncommitted"
        }
        else {
            props[ConsumerConfig.GROUP_ID_CONFIG] = "group-CLEAN-read-${consumerId}"
            props[ConsumerConfig.ISOLATION_LEVEL_CONFIG] = "read_committed"
        }
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        props[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = true
        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "latest"

        val consumer:KafkaConsumer<String, String> = KafkaConsumer<String, String>(props)
        consumer.subscribe(Collections.singletonList(INPUT_TOPIC))

        return consumer

    }

    fun readClean(): ConsumerRecords<String, String> {

        var records: ConsumerRecords<String, String> = consumer.poll(Duration.ofMillis(10000))

        System.err.println("CLEAN READ ${records.count()} records")

        records.iterator().forEach {
            System.err.println("->    ${it.value()}")
        }

        return records
    }

    fun readDirty(): ConsumerRecords<String, String> {

        var records: ConsumerRecords<String, String> = dirtConsumer.poll(Duration.ofMillis(10000))

        System.err.println("DIRT READ ${records.count()} records")

        records.iterator().forEach {
            System.err.println("->    ${it.value()}")
        }

        return records
    }




}