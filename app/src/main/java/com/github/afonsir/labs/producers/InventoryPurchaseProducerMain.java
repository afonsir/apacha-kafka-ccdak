package com.github.afonsir.labs.producers;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class InventoryPurchaseProducerMain {
    public static void main(String[] args) {
        String bootstrapServers = "localhost:29092";

        // Producer configs
        Properties properties = new Properties();

        properties.setProperty( ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers );

        // Serializers
        properties.setProperty( ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName() );
        properties.setProperty( ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName() );

        // Acknowledgements
        properties.setProperty( ProducerConfig.ACKS_CONFIG, "all" );

        // create producer
        Producer<String, String> producer = new KafkaProducer<String, String>( properties );

        try {
            File file = new File( InventoryPurchaseProducerMain.class.getClassLoader().getResource( "sample_transaction_log.txt" ).getFile() );
            BufferedReader br = new BufferedReader( new FileReader(file) );
            String line;

            while ( (line = br.readLine()) != null ) {
                String[] lineArray = line.split(":");

                String key   = lineArray[0];
                String value = lineArray[1];

                producer.send( new ProducerRecord<String, String>( "inventory-purchases", key, value ) );

                if ( key.equals("apples") ) {
                    producer.send( new ProducerRecord<String, String>( "apple-purchases", key, value ) );
                }
            }
            br.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        producer.close();
    }
}
