package com.github.afonsir.kafka;

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

        // Consumer configs
        Properties properties = new Properties();

        properties.setProperty( ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers );
        properties.setProperty( ConsumerConfig.GROUP_ID_CONFIG, "first-group" );
        properties.setProperty( ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName() );
        properties.setProperty( ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName() );
        properties.setProperty( ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false" );

        // Create Consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>( properties );

        consumer.subscribe(Arrays.asList( "first-topic", "second-topic" ));

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll( Duration.ofMillis(100) );

            for ( ConsumerRecord<String, String> record : records ) {
                System.out.println( "key="       + record.key()       + ", " +
                                    "value="     + record.value()     + ", " +
                                    "topic="     + record.topic()     + ", " +
                                    "partition=" + record.partition() + ", " +
                                    "offset="    + record.offset());
            }

            // manual batch message commit
            consumer.commitSync();
        }
    }

}
