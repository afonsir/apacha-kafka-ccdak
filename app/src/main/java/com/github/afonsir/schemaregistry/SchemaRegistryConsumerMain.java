package com.github.afonsir.schemaregistry;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class SchemaRegistryConsumerMain {
    public static void main(String[] args) {
        String bootstrapServers = "localhost:29092";

        // Consumer configs
        Properties properties = new Properties();

        properties.setProperty( ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers );
        properties.setProperty( ConsumerConfig.GROUP_ID_CONFIG, "schema-group" );
        properties.setProperty( ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true" );
        properties.setProperty( ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, Integer.toString(1000) );
        properties.setProperty( ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest" );

        // Serialization with Avro
        properties.setProperty( AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:38081" );

        properties.setProperty( ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName() );
        properties.setProperty( ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName() );

        properties.setProperty( KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true" );

        // Create Consumer
        KafkaConsumer<String, Person> consumer = new KafkaConsumer<String, Person>( properties );

        consumer.subscribe( Collections.singletonList( "employees" ));

        while (true) {
            final ConsumerRecords<String, Person> records = consumer.poll( Duration.ofMillis(100) );

            for ( ConsumerRecord<String, Person> record : records ) {
                final String key   = record.key();
                final Person value = record.value();

                System.out.println( "key=" + key + ", value=" + value );
            }
        }
    }

}
