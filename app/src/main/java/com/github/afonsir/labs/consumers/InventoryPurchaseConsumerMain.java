package com.github.afonsir.labs.consumers;

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

        // Deserializers
        properties.setProperty( ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName() );
        properties.setProperty( ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName() );

        // Create Consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>( properties );

        consumer.subscribe(Arrays.asList( "inventory-purchases" ));

        try {
          BufferedWriter writer = new BufferedWriter(new FileWriter( "/home/cloud_user/output/output.dat", true ));

          while (true) {
              ConsumerRecords<String, String> records = consumer.poll( Duration.ofMillis(100) );

              for ( ConsumerRecord<String, String> record : records ) {
                  String recordString = "key="       + record.key()       + ", " +
                                        "value="     + record.value()     + ", " +
                                        "topic="     + record.topic()     + ", " +
                                        "partition=" + record.partition() + ", " +
                                        "offset="    + record.offset();

                  System.out.println( recordString );

                  writer.write( recordString + "\n" );
              }

              // manual batch message commit
              consumer.commitSync();

              writer.flush();
          }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
