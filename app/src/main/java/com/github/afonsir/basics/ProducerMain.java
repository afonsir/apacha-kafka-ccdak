package com.github.afonsir.basics;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class ProducerMain {
    public static void main(String[] args) {
        String bootstrapServers = "localhost:29092";

        // Producer configs
        Properties properties = new Properties();

        properties.setProperty( ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers );
        properties.setProperty( ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName() );
        properties.setProperty( ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName() );

        // create producer
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>( properties );

        for ( int i = 0; i <= 100; i++ ) {
            producer.send( new ProducerRecord<String, String>( "count-topic", "count", Integer.toString(i) ) );
        }

        producer.close();
    }
}
