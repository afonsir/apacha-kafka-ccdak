package com.github.afonsir.testing;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;

/**
 *
 * @author afonso
 */
public class MyStreams {

    final KafkaStreams streams;
    final Topology topology;

    public MyStreams() {
        final Properties props = new Properties();

        props.put( StreamsConfig.APPLICATION_ID_CONFIG, "streams-example" );
        props.put( StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092" );
        props.put( StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0 );
        props.put( StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Integer().getClass().getName() );
        props.put( StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName() );

        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<Integer, String> source = builder.stream( "test-input-topic" );

        source
            .mapValues(( value ) -> {
                String reverse = "";

                for( int i = value.length() - 1; i >= 0; i-- ) {
                    reverse = reverse + value.charAt(i);
                }

                return reverse;
            })
            .to( "test-output-topic" );

        topology = builder.build();

        streams = new KafkaStreams( topology, props );
    }

    public void run() {
        // Print the topology to the console.
        System.out.println( topology.describe() );

        final CountDownLatch latch = new CountDownLatch(1);

        // Attach a shutdown handler to catch control-c and terminate the application gracefully.
        Runtime.getRuntime().addShutdownHook( new Thread( "streams-shutdown-hook" ) {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();
            latch.await();
        } catch (final Throwable e) {
            System.out.println( e.getMessage() );
            System.exit(1);
        }

        System.exit(0);
    }
}
