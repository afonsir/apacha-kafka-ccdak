package com.github.afonsir.labs.schemaregistry;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.time.Duration;

import java.util.Arrays;
import java.util.Properties;

public class InventoryPurchaseConsumerMain {
    public static void main(String[] args) {
        String bootstrapServers = "localhost:29092";

        // Consumer configs
        Properties properties = new Properties();

        properties.setProperty( ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers );
        properties.setProperty( ConsumerConfig.GROUP_ID_CONFIG, "inventory-purchase-group" );
        properties.setProperty( ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true" );
        properties.setProperty( ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, Integer.toString(1000) );
        properties.setProperty( ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest" );

        // Serialization with Avro
        properties.setProperty( AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:38081" );

        properties.setProperty( ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName() );
        properties.setProperty( ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName() );

        properties.setProperty( KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true" );

        // Create Consumer
        KafkaConsumer<String, Purchase> consumer = new KafkaConsumer<String, Purchase>( properties );

        consumer.subscribe( Arrays.asList( "inventory-purchases" ));

        try {
          BufferedWriter writer = new BufferedWriter( new FileWriter( "/home/cloud_user/output/output.txt", true ));

          while (true) {
              ConsumerRecords<String, Purchase> records = consumer.poll( Duration.ofMillis(100) );

              for ( ConsumerRecord<String, Purchase> record : records ) {
                  final String key     = record.key();
                  final Purchase value = record.value();

                  final String recordString = "key=" + key + ", value=" + value;

                  System.out.println( recordString );

                  writer.write( recordString + "\n" );
              }

              writer.flush();
          }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
