package com.github.afonsir.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
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
        properties.setProperty( ProducerConfig.ACKS_CONFIG, "all" );

        // create producer
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>( properties );

        for ( int i = 0; i <= 100; i++ ) {
            int partition = ( i > 49 ) ? 1 : 0;

            ProducerRecord record = new ProducerRecord<String, String>( "count-topic", partition, "count", Integer.toString(i) );

            producer.send( record, ( RecordMetadata metadata, Exception e ) -> {
                if ( e != null ) {
                    System.out.println( "Error publishing message: " + e.getMessage() );
                } else {
                    System.out.println( "Published message: " +
                        "key="       + record.key()         + ", " +
                        "value="     + record.value()       + ", " +
                        "topic="     + metadata.topic()     + ", " +
                        "partition=" + metadata.partition() + ", " +
                        "offset="    + metadata.offset());
                }
            });
        }

        producer.close();
    }
}
