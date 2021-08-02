package com.github.afonsir.labs.schemaregistry;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class InventoryPurchaseProducerMain {
    public static void main(String[] args) {
        String bootstrapServers = "localhost:29092";

        // Producer configs
        Properties properties = new Properties();

        properties.setProperty( ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers );
        properties.setProperty( ProducerConfig.ACKS_CONFIG, "all" );
        properties.setProperty( ProducerConfig.RETRIES_CONFIG, Integer.toString(0) );

        // Serialization with Avro
        properties.setProperty( AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:28081" );

        properties.setProperty( ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName() );
        properties.setProperty( ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName() );


        // create producer
        Producer<String, Purchase> producer = new KafkaProducer<String, Purchase>( properties );

        Purchase purchaseOne = new Purchase( 101, "Apple", 5 );
        producer.send( new ProducerRecord<String, Purchase>( "inventory-purchases", purchaseOne.getId().toString(), purchaseOne ));

        Purchase purchaseTwo = new Purchase( 102, "Lemon", 2 );
        producer.send( new ProducerRecord<String, Purchase>( "inventory-purchases", purchaseTwo.getId().toString(), purchaseTwo ));

        Purchase purchaseThree = new Purchase( 103, "Wattermelon", 1 );
        producer.send( new ProducerRecord<String, Purchase>( "inventory-purchases", purchaseThree.getId().toString(), purchaseThree ));

        Purchase purchaseFour = new Purchase( 104, "Grape", 12 );
        producer.send( new ProducerRecord<String, Purchase>( "inventory-purchases", purchaseFour.getId().toString(), purchaseFour ));

        Purchase purchaseFive = new Purchase( 105, "Banana", 6 );
        producer.send( new ProducerRecord<String, Purchase>( "inventory-purchases", purchaseFive.getId().toString(), purchaseFive ));

        producer.close();
    }
}
