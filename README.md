## Installing Kafka Confluent in debian systems

- Import the apt key:

```bash
wget --quiet --output-document - https://packages.confluent.io/deb/5.2/archive.key | sudo apt-key add -
```

- Add Confluent repository:

```bash
sudo add-apt-repository "deb [arch=amd64] https://packages.confluent.io/deb/5.2/ stable main"
```

- Install Kafka Confluent and its dependencies:

```bash
sudo apt-get update && sudo apt-get install --quiet --yes openjdk-8-jdk confluent-community-2.12
```

- Configure the host entries:

```bash
# /etc/hosts

<PRIVATE_IP_BROKER_1> zoo-1 kafka-1
<PRIVATE_IP_BROKER_2> zoo-2 kafka-2
<PRIVATE_IP_BROKER_3> zoo-3 kafka-3
```

- Configure Zookeeper:

```bash
# /etc/kafka/zookeeper.properties

tickTime=2000
dataDir=/var/lib/zookeeper/
clientPort=2181
initLimit=5
syncLimit=2
server.1=zoo-1:2888:3888
server.2=zoo-2:2888:3888
server.3=zoo-3:2888:3888
autopurge.snapRetainCount=3
autopurge.purgeInterval=24
```

- Configure Zookeeper ID file:

```bash
# /var/lib/zookeeper/myid

[ZOOKEEPER_ID] # ex. 1, 2, 3 and so on.
```

- Enable Zookeeper service:

```bash
sudo systemctl enable confluent-zookeeper
```

- Start Zookeeper service:

```bash
sudo systemctl start confluent-zookeeper
```

- Configure Kafka:

```bash
# /etc/kafka/server.properties

broker.id=[BROKER_ID] # ex. 1, 2, 3 and so on.
advertised.listeners=PLAINTEXT://kafka-[BROKER_ID]:9092
zookeeper.connect=zoo-[BROKER_ID]:2181
```

- Enable Kafka service:

```bash
sudo systemctl enable confluent-kafka
```

- Start Zookeeper service:

```bash
sudo systemctl start confluent-kafka
```

- Check the services status:

```bash
sudo systemctl status confluent*
```

- Check broker cluster listing topics:

```bash
kafka-topics \
  --bootstrap-server kafka-[BROKER_ID]:9092 \
  --list
```

## Create a Producer and a Consumer

- Create the topic *inventory_purchases*:

```bash
kafka-topics \
  --bootstrap-server localhost:9092 \
  --create \
  --topic inventory_purchases \
  --replication-factor 3 \
  --partitions 6
```

- Create the producer and add some messages:

```bash
kafka-console-producer \
  --broker-list localhost:9092 \
  --topic inventory_purchases

> product: apples, quantity: 5
> product: lemons, quantity: 7
> product: bananas, quantity: 3
```

- Create the consumer:

```bash
kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic inventory_purchases \
  --from-beginning
```

## Consuming from a Consumer Group

- Create a Consumer Group, with one consumer:

```bash
kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic inventory_purchases \
  --group 1 > /tmp/group1_consumer1.txt
```

- Create another Consumer Group, with two consumers:

```bash
# In one session

kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic inventory_purchases \
  --group 2 > /tmp/group2_consumer1.txt

# In another session

kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic inventory_purchases \
  --group 2 > /tmp/group2_consumer2.txt
```

## Data Streaming

- Create the topic *streams-input-topic*:

```bash
kafka-topics \
  --bootstrap-server localhost:29092 \
  --create \
  --topic streams-input-topic \
  --replication-factor 3 \
  --partitions 3
```

- Create the topic *streams-output-topic*:

```bash
kafka-topics \
  --bootstrap-server localhost:39092 \
  --create \
  --topic streams-output-topic \
  --replication-factor 3 \
  --partitions 3
```

- Start Stream Java project:

```bash
./gradlew run
```

- Create a consumer for *streams-output-topic*:

```bash
kafka-console-consumer \
  --bootstrap-server localhost:39092 \
  --topic streams-output-topic \
  --property print.key=true
```

- Create a producer for *streams-input-topic*:

```bash
kafka-console-producer \
  --broker-list localhost:29092 \
  --topic streams-input-topic \
  --property parse.key=true \
  --property key.separator=:

# To add messages
>key1:value1
>key2:value2
>key3:value3
```

## Stateless Transformations

- Create the topic *stateless-transformations-input-topic*:

```bash
kafka-topics \
  --bootstrap-server localhost:29092 \
  --create \
  --topic stateless-transformations-input-topic \
  --replication-factor 3 \
  --partitions 3
```

- Create the topic *stateless-transformations-output-topic*:

```bash
kafka-topics \
  --bootstrap-server localhost:39092 \
  --create \
  --topic stateless-transformations-output-topic \
  --replication-factor 3 \
  --partitions 3
```

- Start StatelessTransformations Java project:

```bash
./gradlew runStatelessTransformations
```

- Create a consumer for *stateless-transformations-output-topic*:

```bash
kafka-console-consumer \
  --bootstrap-server localhost:39092 \
  --topic stateless-transformations-output-topic \
  --property print.key=true
```

- Create a producer for *stateless-transformations-input-topic*:

```bash
kafka-console-producer \
  --broker-list localhost:29092 \
  --topic stateless-transformations-input-topic \
  --property parse.key=true \
  --property key.separator=:

# To add messages
>akey:avalue
>akey:avalue
>akey:bvalue
>bkey:bvalue
```

## Aggregation

- Create the topic *aggregations-input-topic*:

```bash
kafka-topics \
  --bootstrap-server localhost:29092 \
  --create \
  --topic aggregations-input-topic \
  --replication-factor 3 \
  --partitions 3
```

- Create the topic *aggregations-output-character-count-topic*:

```bash
kafka-topics \
  --bootstrap-server localhost:39092 \
  --create \
  --topic aggregations-output-character-count-topic \
  --replication-factor 3 \
  --partitions 3
```

- Create the topic *aggregations-output-count-topic*:

```bash
kafka-topics \
  --bootstrap-server localhost:39092 \
  --create \
  --topic aggregations-output-count-topic \
  --replication-factor 3 \
  --partitions 3
```

- Create the topic *aggregations-output-reduce-topic*:

```bash
kafka-topics \
  --bootstrap-server localhost:39092 \
  --create \
  --topic aggregations-output-reduce-topic \
  --replication-factor 3 \
  --partitions 3
```

- Start AggregationsMain Java project:

```bash
./gradlew runAggregations
```

- Create a consumer for *aggregations-output-character-count-topic*:

```bash
kafka-console-consumer \
  --bootstrap-server localhost:39092 \
  --topic aggregations-output-character-count-topic \
  --property print.key=true \
  --property value.deserializer=org.apache.kafka.common.serialization.IntegerDeserializer
```

- Create a consumer for *aggregations-output-count-topic*:

```bash
kafka-console-consumer \
  --bootstrap-server localhost:39092 \
  --topic aggregations-output-count-topic \
  --property print.key=true \
  --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
```

- Create a consumer for *aggregations-output-reduce-topic*:

```bash
kafka-console-consumer \
  --bootstrap-server localhost:39092 \
  --topic aggregations-output-reduce-topic \
  --property print.key=true
```

- Create a producer for *aggregations-input-topic*:

```bash
kafka-console-producer \
  --broker-list localhost:29092 \
  --topic aggregations-input-topic \
  --property parse.key=true \
  --property key.separator=:

# To add messages
>b:hello
>b:world
>c:hello
```