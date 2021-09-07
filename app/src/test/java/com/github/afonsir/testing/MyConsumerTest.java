package com.github.afonsir.testing;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.TopicPartition;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author afonso
 */
public class MyConsumerTest {

    MockConsumer<Integer, String> mockConsumer;
    MyConsumer myConsumer;

    // Contains data sent so System.out during the test.
    private ByteArrayOutputStream systemOutContent;
    private final PrintStream originalSystemOut = System.out;

    @Before
    public void setUp() {
        mockConsumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
        myConsumer = new MyConsumer();
        myConsumer.consumer = mockConsumer;
    }

    @Before
    public void setUpStreams() {
        systemOutContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(systemOutContent));
    }

    @After
    public void restoreStreams() {
        System.setOut(originalSystemOut);
    }

    @Test
    public void testHandleRecordsOutput() {
        String topic = "test-topic";
        int partition = 0;
        int offset = 1;

        int recordKey = 2;
        String recordValue = "Test Value";

        String expectedReturn = "key="       + recordKey + ", " +
                                "value="     + recordValue + ", " +
                                "topic="     + topic + ", " +
                                "partition=" + partition + ", " +
                                "offset="    + offset + "\n";

        ConsumerRecord<Integer, String> record = new ConsumerRecord<>(
            topic,
            partition,
            offset,
            recordKey,
            recordValue
        );

        mockConsumer.assign( Arrays.asList( new TopicPartition( topic, partition )));
        HashMap<TopicPartition, Long> beginningOffsets = new HashMap<>();
        beginningOffsets.put( new TopicPartition( topic, partition ), 0L );
        mockConsumer.updateBeginningOffsets( beginningOffsets );

        mockConsumer.addRecord( record );

        myConsumer.handleRecords();
        Assert.assertEquals( systemOutContent.toString(), expectedReturn );
    }
}
