package com.github.afonsir.streams;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;

import java.util.Properties;
import java.util.List;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

public class StatelessTransformationsMain {
    public static void main(String[] args) {
        String bootstrapServers = "localhost:29092";

        // Stream configs
        final Properties properties = new Properties();

        properties.setProperty( StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers );
        properties.setProperty( StreamsConfig.APPLICATION_ID_CONFIG, "stateless-transformations-example" );
        properties.setProperty( StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, Integer.toString(0) );

        // Serializers
        properties.setProperty( StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName() );
        properties.setProperty( StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName() );

        // Get the source stream.
        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, String> source = builder.stream( "stateless-transformations-input-topic" );

        // Split the stream into two streams, one containing all records where the key
        // begins with "a", and the other containing all other records.
        KStream<String, String>[] branches = source.branch( (key, value) -> key.startsWith("a"), (key, value) -> true );

        KStream<String, String> aKeysStream = branches[0];
        KStream<String, String> othersStream = branches[1];

        // filter records that don't starts with "a"
        aKeysStream = aKeysStream.filter( (key, value) -> value.startsWith("a") );

        // convert each record to a uppercased and a loweredcase values.
        aKeysStream = aKeysStream.flatMap( (key, value) -> {
            List<KeyValue<String, String>> result = new LinkedList<>();

            result.add( KeyValue.pair(key, value.toUpperCase()) );
            result.add( KeyValue.pair(key, value.toLowerCase()) );

            return result;
        } );

        // uppercase keys
        aKeysStream = aKeysStream.map( (key, value) -> KeyValue.pair( key.toUpperCase(), value ) );

        // merge the two streams back together
        KStream<String, String> mergedStream = aKeysStream.merge( othersStream );

        // print each record to the console
        mergedStream = mergedStream.peek( (key, value) -> System.out.println( "key=" + key + ", value=" + value ) );

        mergedStream.to( "stateless-transformations-output-topic" );

        final Topology topology = builder.build();
        final KafkaStreams streams = new KafkaStreams( topology, properties );

        // Print the topology to the console
        System.out.println( topology.describe() );

        final CountDownLatch latch = new CountDownLatch(1);

        // Attach a shutdown handler to catch control-c and terminate the application gracefully
        Runtime.getRuntime().addShutdownHook(new Thread( "streams-shutdown-hook" ) {
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
            System.out.println(e.getMessage());
            System.exit(1);
        }

        System.exit(0);
    }
}
