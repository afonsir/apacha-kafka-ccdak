package com.github.afonsir.streams;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.KStream;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class JoinsMain {
    public static void main(String[] args) {
        String bootstrapServers = "localhost:29092";

        // Stream configs
        final Properties properties = new Properties();

        properties.setProperty( StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers );
        properties.setProperty( StreamsConfig.APPLICATION_ID_CONFIG, "joins-example" );
        properties.setProperty( StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, Integer.toString(0) );

        // Serializers
        properties.setProperty( StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName() );
        properties.setProperty( StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName() );

        // Get the source stream.
        final StreamsBuilder builder = new StreamsBuilder();

        KStream<String, String> left = builder.stream( "joins-input-topic-left" );
        KStream<String, String> right = builder.stream( "joins-input-topic-right" );

        // Perform an inner join
        KStream<String, String> innerJoined = left.join(
            right,
            ( leftValue, rightValue ) -> "left=" + leftValue + ", right=" + rightValue,
            JoinWindows.of( Duration.ofMinutes(5) )
        );

        innerJoined.to( "inner-join-output-topic" );

        // Perform a left join
        KStream<String, String> leftJoined = left.leftJoin(
            right,
            ( leftValue, rightValue ) -> "left=" + leftValue + ", right=" + rightValue,
            JoinWindows.of( Duration.ofMinutes(5) )
        );

        leftJoined.to( "left-join-output-topic" );

        // Perform an outer join
        KStream<String, String> outerJoined = left.outerJoin(
            right,
            ( leftValue, rightValue ) -> "left=" + leftValue + ", right=" + rightValue,
            JoinWindows.of( Duration.ofMinutes(5) )
        );

        outerJoined.to( "outer-join-output-topic" );

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
