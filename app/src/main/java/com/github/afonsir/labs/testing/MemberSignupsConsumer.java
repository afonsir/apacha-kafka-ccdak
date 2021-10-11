package com.github.afonsir.testing;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

/**
 *
 * @author afonso
 */
public class MemberSignupsConsumer {

    Consumer<Integer, String> consumer;

    public MemberSignupsConsumer() {
        Properties props = new Properties();

        props.setProperty( ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092" );
        props.setProperty( ConsumerConfig.GROUP_ID_CONFIG, "group1" );
        props.setProperty( ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class.getName() );
        props.setProperty( ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName() );
        props.setProperty( ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false" );

        consumer = new KafkaConsumer<>( props );
        consumer.subscribe( Arrays.asList( "test-topic1", "test-topic2" ));
    }

    public void run() {

        while ( true ) {
            ConsumerRecords<Integer, String> records = consumer.poll( Duration.ofMillis(100) );
            handleRecords( records );
        }

    }

    public void handleRecords( ConsumerRecords<Integer, String> records ) {
        for ( ConsumerRecord<Integer, String> record : records ) {
            System.out.println( "key="       + record.key() + ", " +
                                "value="     + record.value() + ", " +
                                "topic="     + record.topic() + ", " +
                                "partition=" + record.partition() + ", " +
                                "offset="    + record.offset() );
        }

        consumer.commitSync();
    }
}
