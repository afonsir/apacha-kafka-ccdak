package com.github.afonsir.schemaregistry;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class SchemaRegistryProducerMain {
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
        KafkaProducer<String, Person> producer = new KafkaProducer<String, Person>( properties );

        Person kenny = new Person( 1234, "Kenny", "Armstrong", "kenny@mail.com" );
        producer.send( new ProducerRecord<String, Person>( "employees", kenny.getId().toString(), kenny ));

        Person terry = new Person( 1234, "Terry", "Cox", "terry@mail.com" );
        producer.send( new ProducerRecord<String, Person>( "employees", terry.getId().toString(), terry ));

        producer.close();
    }
}
