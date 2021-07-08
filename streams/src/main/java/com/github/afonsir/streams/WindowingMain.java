package com.github.afonsir.streams;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindowedKStream;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.kstream.WindowedSerdes;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class WindowingMain {
    public static void main(String[] args) {
        String bootstrapServers = "localhost:29092";

        // Stream configs
        final Properties properties = new Properties();

        properties.setProperty( StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers );
        properties.setProperty( StreamsConfig.APPLICATION_ID_CONFIG, "windowing-example" );
        properties.setProperty( StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, Integer.toString(0) );

        // Serializers
        properties.setProperty( StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName() );
        properties.setProperty( StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName() );

        // Get the source stream.
        final StreamsBuilder builder = new StreamsBuilder();

        KStream<String, String> source = builder.stream( "windowing-input-topic" );

        KGroupedStream<String, String> groupedStream = source.groupByKey();

        // apply windowing to the stream with tumbling time windows of seconds
        TimeWindowedKStream<String, String> windowedStream = groupedStream.windowedBy(
            TimeWindows.of( Duration.ofSeconds(10) )
        );

        // combine the values of all records with the same key into a string separated
        // by spaces, using 10-second windows
        KTable<Windowed<String>, String> reducedTable = windowedStream.reduce(
            ( aggValue, newValue ) -> aggValue + " " + newValue
        );

        reducedTable.toStream().to(
            "windowing-output-topic",
            Produced.with( WindowedSerdes.timeWindowedSerdeFrom(String.class), Serdes.String() )
        );

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
