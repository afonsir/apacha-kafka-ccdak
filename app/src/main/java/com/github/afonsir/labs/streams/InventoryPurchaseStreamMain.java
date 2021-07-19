package com.github.afonsir.labs.streams;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class InventoryPurchaseStreamMain {
    public static void main(String[] args) {
        String bootstrapServers = "localhost:9092";

        // Stream configs
        final Properties properties = new Properties();

        properties.setProperty( StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers );
        properties.setProperty( StreamsConfig.APPLICATION_ID_CONFIG, "inventory-purchase-lab" );
        properties.setProperty( StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, Integer.toString(0) );

        // Serializers
        properties.setProperty( StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName() );
        properties.setProperty( StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName() );

        // Get the source stream.
        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, String> source = builder.stream( "inventory_purchases" );

        final KStream<String, Integer> integerValuesSource = source.mapValues( value -> {
            try {
                return Integer.valueOf(value);
            } catch (NumberFormatException e) {
                System.out.println( "Unable to convert to integer: \"" + value + "\"" );
                return 0;
            }
        });

        // group the source stream by the existing Key
        KTable<String, Integer> productCounts = integerValuesSource
            .groupByKey( Grouped.with( Serdes.String(), Serdes.Integer() ))
            .reduce(( total, newQuantity ) -> total + newQuantity );

        productCounts.toStream().to( "total_purchases", Produced.with( Serdes.String(), Serdes.Integer() ));

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
