package com.github.afonsir.services;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
// import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class AggregationsMain {
    public static void main(String[] args) {
        String bootstrapServers = "localhost:29092";

        // Stream configs
        final Properties properties = new Properties();

        properties.setProperty( StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers );
        properties.setProperty( StreamsConfig.APPLICATION_ID_CONFIG, "aggregations-example" );
        properties.setProperty( StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, Integer.toString(0) );

        // Serializers
        properties.setProperty( StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName() );
        properties.setProperty( StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName() );

        // Get the source stream.
        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, String> source = builder.stream( "aggregations-input-topic" );

        // group the source stream by the existing Key
        KGroupedStream<String, String> groupedStream = source.groupByKey();

        // aggregate by value length
        KTable<String, Integer> aggregatedTable = groupedStream.aggregate(
            () -> 0,
            ( aggKey, newValue, aggValue ) -> aggValue + newValue.length(),
            Materialized.with( Serdes.String(), Serdes.Integer() )
        );

        aggregatedTable.toStream().to(
            "aggregations-output-character-count-topic",
            Produced.with( Serdes.String(), Serdes.Integer() )
        );

        // count the number of records for each key
        KTable<String, Long> countedTable = groupedStream.count(Materialized.with( Serdes.String(), Serdes.Long() ));

        countedTable.toStream().to( "aggregations-output-count-topic", Produced.with( Serdes.String(), Serdes.Long() ));

        // combine the values of all records with the same key into a string separeted by spaces
        KTable<String, String> reducedTable = groupedStream.reduce( (aggValue, newValue) -> aggValue + " " + newValue );

        reducedTable.toStream().to( "aggregations-output-reduce-topic" );

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
