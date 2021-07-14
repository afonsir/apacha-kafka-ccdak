package com.github.afonsir.producers;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class InventoryPurchaseMain {
    public static void main(String[] args) {
        String bootstrapServers = "localhost:29092";

        // Producer configs
        Properties properties = new Properties();

        properties.setProperty( ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers );

        // Serializers
        properties.setProperty( ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName() );
        properties.setProperty( ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName() );

        properties.setProperty( ProducerConfig.ACKS_CONFIG, "all" );
        properties.setProperty( ProducerConfig.BUFFER_MEMORY_CONFIG, Integer.toString(12582912) );
        properties.setProperty( ProducerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, Integer.toString(300000) );

        // create producer
        Producer<String, String> producer = new KafkaProducer<>( properties );

        producer.send( new ProducerRecord<>("inventory-purchases", "apples", "1" ));
        producer.send( new ProducerRecord<>("inventory-purchases", "apples", "3" ));
        producer.send( new ProducerRecord<>("inventory-purchases", "oranges", "12" ));
        producer.send( new ProducerRecord<>("inventory-purchases", "bananas", "25" ));
        producer.send( new ProducerRecord<>("inventory-purchases", "pears", "15" ));
        producer.send( new ProducerRecord<>("inventory-purchases", "apples", "6" ));
        producer.send( new ProducerRecord<>("inventory-purchases", "pears", "7" ));
        producer.send( new ProducerRecord<>("inventory-purchases", "oranges", "1" ));
        producer.send( new ProducerRecord<>("inventory-purchases", "grapes", "56" ));
        producer.send( new ProducerRecord<>("inventory-purchases", "oranges", "11" ));

        producer.close();
    }
}
