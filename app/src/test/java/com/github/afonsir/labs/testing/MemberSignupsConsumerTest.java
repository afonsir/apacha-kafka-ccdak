package com.github.afonsir.labs.testing;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.TopicPartition;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MemberSignupsConsumerTest {

    MockConsumer<Integer, String> mockConsumer;
    MemberSignupsConsumer memberSignupsConsumer;

    // Contains data sent so System.out during the test.
    private ByteArrayOutputStream systemOutContent;
    private final PrintStream originalSystemOut = System.out;

    @Before
    public void setUp() {
        mockConsumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
        memberSignupsConsumer = new MemberSignupsConsumer();
        memberSignupsConsumer.consumer = mockConsumer;
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
        // Verify that the testHandleRecords writes the correct data to System.out
        // A text fixture called systemOutContent has already been set up in this class to capture System.out data.
        String topic = "member-signups";
        int partition = 0;
        int offset = 1;

        int recordKey = 2;
        String recordValue = "ROSENBERG, WILLOW";

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


        Map<TopicPartition, List<ConsumerRecord<Integer, String>>> records = new LinkedHashMap<>();

        records.put( new TopicPartition( topic, partition ), Arrays.asList( record ));

        ConsumerRecords<Integer, String> consumerRecords = new ConsumerRecords<>( records );

        memberSignupsConsumer.handleRecords( consumerRecords );

        Assert.assertEquals( systemOutContent.toString(), expectedReturn );
    }

    @Test
    public void testHandleRecordsNone() {
        // Verify that testHandleRecords behaves correctly when processing no records.
        // A text fixture called systemOutContent has already been set up in this class to capture System.out data.
        String topic = "member-signups";
        int partition = 0;

        Map<TopicPartition, List<ConsumerRecord<Integer, String>>> records = new LinkedHashMap<>();

        records.put( new TopicPartition( topic, partition ), Arrays.asList() );

        ConsumerRecords<Integer, String> consumerRecords = new ConsumerRecords<>( records );

        memberSignupsConsumer.handleRecords( consumerRecords );

        Assert.assertEquals( systemOutContent.toString(), "");
    }

    @Test
    public void testHandleRecordsMultiple() {
        // Verify that testHandleRecords behaves correctly when processing multiple records.
        // A text fixture called systemOutContent has already been set up in this class to capture System.out data.
        String topic = "member-signups";

        int partitionOne = 0;
        int partitionTwo = 3;

        int offsetOne = 1;
        int offsetTwo = 4;

        int recordKeyOne = 2;
        String recordValueOne = "ROSENBERG, WILLOW";

        int recordKeyTwo = 5;
        String recordValueTwo = "HARRIS, ALEXANDER";

        String expectedReturnOne = "key="       + recordKeyOne + ", " +
                                   "value="     + recordValueOne + ", " +
                                   "topic="     + topic + ", " +
                                   "partition=" + partitionOne + ", " +
                                   "offset="    + offsetOne + "\n";

        String expectedReturnTwo = "key="       + recordKeyTwo + ", " +
                                   "value="     + recordValueTwo + ", " +
                                   "topic="     + topic + ", " +
                                   "partition=" + partitionTwo + ", " +
                                   "offset="    + offsetTwo + "\n";

        ConsumerRecord<Integer, String> recordOne = new ConsumerRecord<>(
            topic,
            partitionOne,
            offsetOne,
            recordKeyOne,
            recordValueOne
        );

        ConsumerRecord<Integer, String> recordTwo = new ConsumerRecord<>(
            topic,
            partitionTwo,
            offsetTwo,
            recordKeyTwo,
            recordValueTwo
        );

        Map<TopicPartition, List<ConsumerRecord<Integer, String>>> records = new LinkedHashMap<>();

        records.put( new TopicPartition( topic, partitionOne ), Arrays.asList( recordOne, recordTwo ));

        ConsumerRecords<Integer, String> consumerRecords = new ConsumerRecords<>( records );

        memberSignupsConsumer.handleRecords( consumerRecords );

        Assert.assertEquals( systemOutContent.toString(), expectedReturnOne + expectedReturnTwo );
    }
}
