package com.github.afonsir.basics;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class ConsumerMain {
    public static void main(String[] args) {
        String bootstrapServers = "localhost:29092";

        // Producer configs
        Properties properties = new Properties();

        properties.setProperty( ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers );
        properties.setProperty( ConsumerConfig.GROUP_ID_CONFIG, "my-group" );
        properties.setProperty( ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName() );
        properties.setProperty( ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName() );
        properties.setProperty( ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true" );
        properties.setProperty( ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, Integer.toString(1000) );

        // Create Consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>( properties );

        consumer.subscribe(Arrays.asList("inventory_purchases"));

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll( Duration.ofMillis(100) );

            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
            }
        }
    }

}
