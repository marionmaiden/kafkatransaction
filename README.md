# Handling Transactions on Kafka:

Since Kafka 0.11 version we have support for exactly once semantics (meaning that the produced messages will be delivered only once) which brings us the same capabilities of ACID transactions from DBMS to Kafka.

Under this folder we have two POC projects exploring the kafka producer transaction capabilities:
- kafka_transaction: project using plain KafkaProducer and many test scenarios, simulating exceptions and timeouts within the transaction block
- kafkaTransactionSpring: project using Spring Kafka and many test scenarios, simulating exceptions and timeouts within the transaction block

Please find attached the docker-compose to run the tests

## Documentation Kafka Transaction:

- Exactly once Processing
  - https://www.confluent.io/blog/transactions-apache-kafka/
  - https://www.confluent.io/blog/exactly-once-semantics-are-possible-heres-how-apache-kafka-does-it/

- Exactly once Processing in Java
  - https://www.baeldung.com/kafka-exactly-once
  - https://stackoverflow.com/questions/56460688/kafka-ignoring-transaction-timeout-ms-for-producer
  - https://stackoverflow.com/questions/63596919/spring-kafka-transaction-no-transaction-is-in-process-run-the-template-operat

- Kafka Transactions on Spring Cloud
  - https://spring.io/blog/2023/09/27/introduction-to-transactions-in-spring-cloud-stream-kafka-applications

- Transactional Consumer/Producer
  - https://github.com/eugenp/tutorials/blob/master/apache-kafka/src/main/java/com/baeldung/kafka/exactlyonce/TransactionalWordCount.java


- Docker Kafka documentation
  - https://docs.confluent.io/platform/current/installation/docker/image-reference.html
  - https://docs.confluent.io/platform/current/installation/docker/config-reference.html#confluent-enterprise-ak-configuration
