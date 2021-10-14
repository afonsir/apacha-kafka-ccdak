package com.github.afonsir.labs.testing;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

/**
 *
 * @author afonso
 */
public class MemberSignupsProducer {

    Producer<Integer, String> producer;

    public MemberSignupsProducer() {
        Properties props = new Properties();

        props.setProperty( ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092" );
        props.setProperty( ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName() );
        props.setProperty( ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName() );
        props.setProperty( ProducerConfig.ACKS_CONFIG, "all" );

        props.setProperty( ProducerConfig.RETRIES_CONFIG, Integer.toString(3) );
        props.setProperty( ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, Integer.toString(1) );

        props.setProperty( ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(65536) ); // 64 kb

        producer = new KafkaProducer<>(props);
    }

    public void handleMemberSignup( Integer memberId, String name ) {
        int partition;

        if ( name.toUpperCase().charAt(0) <= 'M' ) {
            partition = 0;
        } else {
            partition = 1;
        }

        ProducerRecord record = new ProducerRecord<>( "member-signups", partition, memberId, name.toUpperCase() );

        producer.send( record, (RecordMetadata recordMetadata, Exception e) -> {
            if (e != null) {
                System.err.println( e.getMessage() );
            } else {
                System.out.println( "key=" + record.key() + ", value=" + record.value() );
            }
        });
    }

    public void tearDown() {
        producer.close();
    }
}
