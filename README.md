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

- Start Kafka service:

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

- Create the topic _inventory_purchases_:

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

- Create the topic _streams-input-topic_:

```bash
kafka-topics \
  --bootstrap-server localhost:29092 \
  --create \
  --topic streams-input-topic \
  --replication-factor 3 \
  --partitions 3
```

- Create the topic _streams-output-topic_:

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

- Create a consumer for _streams-output-topic_:

```bash
kafka-console-consumer \
  --bootstrap-server localhost:39092 \
  --topic streams-output-topic \
  --property print.key=true
```

- Create a producer for _streams-input-topic_:

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

- Create the topic _stateless-transformations-input-topic_:

```bash
kafka-topics \
  --bootstrap-server localhost:29092 \
  --create \
  --topic stateless-transformations-input-topic \
  --replication-factor 3 \
  --partitions 3
```

- Create the topic _stateless-transformations-output-topic_:

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
# streams

./gradlew runStatelessTransformations
```

- Create a consumer for _stateless-transformations-output-topic_:

```bash
kafka-console-consumer \
  --bootstrap-server localhost:39092 \
  --topic stateless-transformations-output-topic \
  --property print.key=true
```

- Create a producer for _stateless-transformations-input-topic_:

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

- Create the topic _aggregations-input-topic_:

```bash
kafka-topics \
  --bootstrap-server localhost:29092 \
  --create \
  --topic aggregations-input-topic \
  --replication-factor 3 \
  --partitions 3
```

- Create the topic _aggregations-output-character-count-topic_:

```bash
kafka-topics \
  --bootstrap-server localhost:39092 \
  --create \
  --topic aggregations-output-character-count-topic \
  --replication-factor 3 \
  --partitions 3
```

- Create the topic _aggregations-output-count-topic_:

```bash
kafka-topics \
  --bootstrap-server localhost:39092 \
  --create \
  --topic aggregations-output-count-topic \
  --replication-factor 3 \
  --partitions 3
```

- Create the topic _aggregations-output-reduce-topic_:

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
# streams

./gradlew runAggregations
```

- Create a consumer for _aggregations-output-character-count-topic_:

```bash
kafka-console-consumer \
  --bootstrap-server localhost:39092 \
  --topic aggregations-output-character-count-topic \
  --property print.key=true \
  --property value.deserializer=org.apache.kafka.common.serialization.IntegerDeserializer
```

- Create a consumer for _aggregations-output-count-topic_:

```bash
kafka-console-consumer \
  --bootstrap-server localhost:39092 \
  --topic aggregations-output-count-topic \
  --property print.key=true \
  --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
```

- Create a consumer for _aggregations-output-reduce-topic_:

```bash
kafka-console-consumer \
  --bootstrap-server localhost:39092 \
  --topic aggregations-output-reduce-topic \
  --property print.key=true
```

- Create a producer for _aggregations-input-topic_:

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

## Joins

- Create the topic _joins-input-topic-left_:

```bash
kafka-topics \
  --bootstrap-server localhost:29092 \
  --create \
  --topic joins-input-topic-left \
  --replication-factor 3 \
  --partitions 3
```

- Create the topic _joins-input-topic-right_:

```bash
kafka-topics \
  --bootstrap-server localhost:39092 \
  --create \
  --topic joins-input-topic-right \
  --replication-factor 3 \
  --partitions 3
```

- Create the topic _inner-join-output-topic_:

```bash
kafka-topics \
  --bootstrap-server localhost:39092 \
  --create \
  --topic inner-join-output-topic \
  --replication-factor 3 \
  --partitions 3
```

- Create the topic _left-join-output-topic_:

```bash
kafka-topics \
  --bootstrap-server localhost:39092 \
  --create \
  --topic left-join-output-topic \
  --replication-factor 3 \
  --partitions 3
```

- Create the topic _outer-join-output-topic_:

```bash
kafka-topics \
  --bootstrap-server localhost:39092 \
  --create \
  --topic outer-join-output-topic \
  --replication-factor 3 \
  --partitions 3
```

- Start JoinsMain Java project:

```bash
# streams

./gradlew runJoins
```

- Create a consumer for _inner-join-output-topic_:

```bash
kafka-console-consumer \
  --bootstrap-server localhost:29092 \
  --topic inner-join-output-topic \
  --property print.key=true
```

- Create a consumer for _left-join-output-topic_:

```bash
kafka-console-consumer \
  --bootstrap-server localhost:39092 \
  --topic left-join-output-topic \
  --property print.key=true
```

- Create a consumer for _outer-join-output-topic_:

```bash
kafka-console-consumer \
  --bootstrap-server localhost:49092 \
  --topic outer-join-output-topic \
  --property print.key=true
```

- Create a producer for _joins-input-topic-left_:

```bash
kafka-console-producer \
  --broker-list localhost:29092 \
  --topic joins-input-topic-left \
  --property parse.key=true \
  --property key.separator=:

# To add messages
>a:a
>b:foo
```

- Create a producer for _joins-input-topic-right_:

```bash
kafka-console-producer \
  --broker-list localhost:29092 \
  --topic joins-input-topic-right \
  --property parse.key=true \
  --property key.separator=:

# To add messages
>a:a
>b:bar
```

## Windowing

- Create the topic _windowing-input-topic_:

```bash
kafka-topics \
  --bootstrap-server localhost:29092 \
  --create \
  --topic windowing-input-topic \
  --replication-factor 3 \
  --partitions 3
```

- Create the topic _windowing-output-topic_:

```bash
kafka-topics \
  --bootstrap-server localhost:39092 \
  --create \
  --topic windowing-output-topic \
  --replication-factor 3 \
  --partitions 3
```

- Start WindowingMain Java project:

```bash
# streams

./gradlew runWindowing
```

- Create a consumer for _windowing-output-topic_:

```bash
kafka-console-consumer \
  --bootstrap-server localhost:29092 \
  --topic windowing-output-topic \
  --property print.key=true
```

- Create a producer for _windowing-input-topic_:

```bash
kafka-console-producer \
  --broker-list localhost:29092 \
  --topic windowing-input-topic \
  --property parse.key=true \
  --property key.separator=:

# To add messages
>a:a
>b:hello
>c:hello
>c:world
# after 10 secods
>c:hello
>c:world
```

## Custom Configurations

- Change the cluster configuration:

```bash
kafka-configs \
  --bootstrap-server localhost:29092 \
  --alter \
  --entity-type brokers \
  --entity-default \
  --add-config retention.ms=259200000
```

- Change a topic configuration:

```bash
kafka-configs \
  --bootstrap-server localhost:29092 \
  --alter \
  --entity-type topics \
  --entity-name inventory-purchases \
  --add-config unclean.leader.election.enable=true, \
               retention.ms=259200000
```

## Producer Metadata

- Create the topic _count-topic_:

```bash
kafka-topics \
  --bootstrap-server localhost:29092 \
  --create \
  --topic count-topic \
  --replication-factor 2 \
  --partitions 2
```

- Start ProducerMain Java project:

```bash
# producer-and-consumer

./gradlew runProducer
```

## Consume from Multiple Topics

- Create the topic _first-topic_:

```bash
kafka-topics \
  --bootstrap-server localhost:29092 \
  --create \
  --topic first-topic \
  --replication-factor 2 \
  --partitions 2
```

- Create the topic _second-topic_:

```bash
kafka-topics \
  --bootstrap-server localhost:29092 \
  --create \
  --topic second-topic \
  --replication-factor 2 \
  --partitions 2
```

```bash
# producer-and-consumer

./gradlew runConsumer
```

- Create a producer for _first-topic_:

```bash
kafka-console-producer \
  --broker-list localhost:29092 \
  --topic first-topic \
  --property parse.key=true \
  --property key.separator=:

# To add messages
>a:a1
>b:foo
>c:hello
```

- Create a producer for _second-topic_:

```bash
kafka-console-producer \
  --broker-list localhost:29092 \
  --topic second-topic \
  --property parse.key=true \
  --property key.separator=:

# To add messages
>a:a2
>b:bar
>c:world
```

## Confluent REST Proxy

- Enable **schema-registry** and **kafka-rest** services:

```bash
sudo systemctl enable confluent-schema-registry confluent-kafka-rest
```

- Start **schema-registry** and **kafka-rest** services:

```bash
sudo systemctl start confluent-schema-registry confluent-kafka-rest
```

### Produce records

- Request to produce records:

```bash
curl \
  --request POST \
  --header 'Content-Type: application/vnd.kafka.json.v2+json' \
  --data-binary @records.json \
  http://localhost:8082/topics/<TOPIC_NAME>
```

- Request body:

```json
// records.json

{
  "records": [
    {
      "key": "<KEY>",
      "value": "VALUE"
    },
    {
      "key": "<KEY>",
      "value": "VALUE"
    }
  ]
}
```

### Consume records

- Create a **consumer** and **consumer-instance**:

```bash
curl \
  --request POST \
  --header 'Content-Type: application/vnd.kafka.json.v2+json' \
  --data-binary @consumer-instance.json \
  http://localhost:8082/consumers/<CONSUMER_NAME>
```

- Request body:

```json
// consumer-instance.json

{
  "name": "<CONSUMER_INSTANCE_NAME>",
  "format": "json",
  "auto.offset.reset": "earliest"
}
```

- Subscribe **consumer instance** to a **topic**:

```bash
curl \
  --request POST \
  --header 'Content-Type: application/vnd.kafka.json.v2+json' \
  --data-binary @topic-subscription.json \
  http://localhost:8082/consumers/<CONSUMER_NAME>/instances/<CONSUMER_INSTANCE_NAME>/subscription
```

- Request body:

```json
// topic-subscription.json

{
  "topics": ["<TOPIC_NAME>"]
}
```

- To consume messages:

```bash
curl \
  --request GET \
  --header 'Content-Type: application/vnd.kafka.json.v2+json' \
  http://localhost:8082/consumers/<CONSUMER_NAME>/instances/<CONSUMER_INSTANCE_NAME>/records
```

- To delete a **consumer instance**:

```bash
curl \
  --request DELETE \
  --header 'Content-Type: application/vnd.kafka.json.v2+json' \
  http://localhost:8082/consumers/<CONSUMER_NAME>/instances/<CONSUMER_INSTANCE_NAME>
```

### Changing Avro Schema

Compatibility types:

- BACKWARD (default)
- BACKWARD_TRANSITIVE (all backward)
- FORWARD (current)
- FORWARD_TRANSITIVE (all forward)
- FULL (current)
- FULL_TRANSITIVE (all)
- NONE

Source: [Confluent Documentation](https://docs.confluent.io/platform/current/schema-registry/avro.html)

## Kafka Connect

Source to Sink connector.

- Start Kafka Connect service:

```bash
sudo systemctl start confluent-kafka-connect
```

### Using with files

- Create **input.txt** file, with some data:

```bash
touch input.txt
```

- Create **output.txt** file, with following permissions:

```bash
touch output.txt && chmod 777 output.txt
```

- Create a source connector:

```bash
curl \
  --request POST \
  --header 'Accept: */*' \
  --header 'Content-Type: application/json' \
  --data-binary @source-connector.json \
  http://localhost:8083/connectors
```

- Request body:

```json
// source-connector.json

{
  "name": "file_source_connector",
  "config": {
    "connector.class": "org.apache.kafka.connect.file.FileStreamSourceConnector",
    "topics": "connector-topic",
    "file": "/home/user/input.txt",
    "value.converter": "org.apache.kafka.connect.storage.StringConverter"
  }
}
```

- Create a sink connector:

```bash
curl \
  --request POST \
  --header 'Accept: */*' \
  --header 'Content-Type: application/json' \
  --data-binary @sink-connector.json \
  http://localhost:8083/connectors
```

- Request body:

```json
// sink-connector.json

{
  "name": "file_sink_connector",
  "config": {
    "connector.class": "org.apache.kafka.connect.file.FileStreamSinkConnector",
    "topics": "connector-topic",
    "file": "/home/user/output.txt",
    "value.converter": "org.apache.kafka.connect.storage.StringConverter"
  }
}
```

- Check the created connector status:

```bash
curl --request GET http://localhost:8083/connectors/<CONNECTOR_NAME>/status
```

- To see the connector metadata:

```bash
curl --request GET http://localhost:8083/connectors/<CONNECTOR_NAME>
```

- To delete a connector:

```bash
curl --request DELETE http://localhost:8083/connectors/<CONNECTOR_NAME>
```

## TLS Encryption

- Create a Certificate Authority (CA):

```bash
openssl req -new \
  -x509 \
  -keyout ca-key \
  -out ca-cert \
  -days 365 \
  -subj "/C=US/ST=Texas/L=Keller/O=Linux Academy/OU=Content/CN=CCDAK"
```

- Create a _client_ keystore:

```bash
keytool \
  -keystore client.truststore.jks \
  -alias CARoot \
  -import \
  -file ca-cert
```

- Create a _server_ keystore:

```bash
keytool \
  -keystore server.truststore.jks \
  -alias CARoot \
  -import \
  -file ca-cert
```

- Create a keystore for each broker server:

```bash
keytool \
  -keystore kafka-[BROKER_ID].truststore.jks \
  -alias localhost \
  -validity 365 \
  -genkey \
  -keyalg RSA \
  -dname "CN=<PUBLIC_DNS_NAME>, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=Unknown" \
  -ext san=dns:kafka-[BROKER_ID],dns:localhost,ip:127.0.0.1,ip:<PRIVATE_IP_ADDR>
```

- Generate a certificate for each broker server:

```bash
keytool \
  -keystore kafka-[BROKER_ID].keystore.jks \
  -alias localhost \
  -certreq \
  -file kafka-[BROKER_ID]-cert-file
```

- Create a configuration file for certificate signing:

```bash
echo subjectAltName = DNS:kafka-[BROKER_ID],DNS:localhost,IP:127.0.0.1,IP:<PRIVATE_IP_ADDR> >> kafka-[BROKER_ID]-extfile.conf
```

- Sign certificates:

```bash
openssl x509 \
  -req \
  -CA ca-cert \
  -CAkey ca-key \
  -in kafka-[BROKER_ID]-cert-file \
  -out kafka-[BROKER_ID]-cert-signed \
  -days 365 \
  -CAcreateserial \
  -extfile kafka-[BROKER_ID]-extfile.conf
```

- Import CA certificate to each broker keystore:

```bash
keytool \
  -keystore kafka-[BROKER_ID].keystore.jks \
  -alias CARoot \
  -import \
  -file ca-cert
```

- Import signed certificates to each broker keystore:

```bash
keytool \
  -keystore kafka-[BROKER_ID].keystore.jks \
  -alias localhost \
  -import \
  -file kafka-[BROKER_ID]-cert-signed
```

- Copy keystores to proper servers:

```bash
# same server
cp kafka-1.keystore.jks server.truststore.jks /home/<USER>

# remote servers
scp [ kafka-2, kafka-3 ].keystore.jks server.truststore.jks <USER>@[ kafka-2, kafka-3 ]:/home/<USER>
```

- Move keystores to proper directories:

```bash
sudo mkdir --parents /var/private/ssl

sudo mv server.truststore.jks /var/private/ssl
sudo mv client.truststore.jks /var/private/ssl
sudo mv kafka-[BROKER_ID].keystore.jks /var/private/ssl/server.keystore.jks

sudo chown --recursive root:root /var/private/ssl
```

- Configure each broker configuration:

```bash
# /etc/kafka/server.properties

listeners=PLAINTEXT://kafka-[BROKER_ID]:9092,SSL://kafka-[BROKER_ID]:9093

# advertised.listeners=PLAINTEXT://kafka-[BROKER_ID]:9092

ssl.keystore.location=/var/private/ssl/server.keystore.jks
ssl.keystore.password=<KEYSTORE_PASSWORD>
ssl.key.password=<BROKER_KEY_PASSWORD>
ssl.truststore.location=/var/private/ssl/server.truststore.jks
ssl.truststore.password=<TRUSTSTORE_PASSWORD>
ssl.client.auth=none
```

- Restart Kafka service:

```bash
sudo systemctl restart confluent-kafka
```

### Testing secure configuration

- Create a properties file:

```bash
# client-ssl.properties

security.protocol=SSL
ssl.truststore.location=/var/private/ssl/client.truststore.jks
ssl.truststore.password=<CLIENT_TRUSTSTORE_PASSWORD>
```

- Use configuration file (using secure **9093** port):

```bash
kafka-console-consumer \
  --bootstrap-server kafka-[BROKER_ID]:9093 \
  --topic tls-test \
  --from-beginning \
  --consumer.config client-ssl.properties
```

## Client Authentication

- Create a keystore for client certificates:

```bash
keytool \
  -keystore client.keystore.jks \
  -alias kafkauser \
  -validity 365 \
  -genkey \
  -keyalg RSA \
  -dname "CN=kafkauser, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=Unknown"
```

- Generate a client certificate:

```bash
keytool \
  -keystore client.keystore.jks \
  -alias kafkauser \
  -certreq \
  -file client-cert-file
```

- Sign certificate:

```bash
openssl x509 \
  -req \
  -CA ca-cert \
  -CAkey ca-key \
  -in client-cert-file \
  -out client-cert-signed \
  -days 365 \
  -CAcreateserial
```

- Import CA certificate to client keystore:

```bash
keytool \
  -keystore client.keystore.jks \
  -alias CARoot \
  -import \
  -file ca-cert
```

- Import signed certificate to client keystore:

```bash
keytool \
  -keystore client.keystore.jks \
  -alias kafkauser \
  -import \
  -file client-cert-signed
```

- Move keystore to proper directory:

```bash
sudo mv client.keystore.jks /var/private/ssl
sudo chown root:root /var/private/ssl/client.keystore.jks
```

- Configure each broker configuration:

```bash
# /etc/kafka/server.properties

ssl.client.auth=required
```

- Restart Kafka service:

```bash
sudo systemctl restart confluent-kafka
```

### Testing client secure configuration

- Update properties file:

```bash
# client-ssl.properties

ssl.keystore.location=/var/private/ssl/client.keystore.jks
ssl.keystore.password=<CLIENT_KEYSTORE_PASSWORD>
ssl.key.password=<CLIENT_KEY_PASSWORD>
```

- Use configuration file (using secure **9093** port):

```bash
kafka-console-consumer \
  --bootstrap-server kafka-[BROKER_ID]:9093 \
  --topic tls-test \
  --from-beginning \
  --consumer.config client-ssl.properties
```

## ACL Authorization

An _Access Control List_ consists of:

- **Principal** - user.
- **Allow/Deny**
- **Operation** - action (read, write, all).
- **Host** - IP address(es).
- **Resource Pattern** - matches one or more resources.

---

- Enable ACL in each broker:

```bash
# /etc/kafka/server.properties

authorizer.class.name=kafka.security.auth.SimpleAclAuthorizer
super.users=User:admin
allow.everyone.if.no.acl.found=true
ssl.principal.mapping.rules=RULE:^CN=(.*?),OU=.*$/$1/,DEFAULT
```

- Restart Kafka service:

```bash
sudo systemctl restart confluent-kafka
```

- To create an ACL:

```bash
kafka-acls \
  --authorizer-properties zookeeper.connect=localhost:2181 \
  --add \
  --allow-principal User:[USER_NAME] \
  --operation all \
  --topic [TOPIC_NAME]
```

- To list topic's ACLs:

```bash
kafka-acls \
  --authorizer-properties zookeeper.connect=localhost:2181 \
  --list \
  --topic [TOPIC_NAME]
```

- To remove an ACL from a topic:

```bash
kafka-acls \
  --authorizer-properties zookeeper.connect=localhost:2181 \
  --remove \
  --topic [TOPIC_NAME]
```

## Monitoring Clients

- Create a producer with JMX enabled:

```bash
KAFKA_JMX_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.local.only=false -Djava.rmi.server.hostname=localhost" \
kafka-console-producer \
  --broker-list localhost:29092 \
  --topic monitor-test

> hello world
> ...
```

- Create a consumer with JMX enabled:

```bash
KAFKA_JMX_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.local.only=false -Djava.rmi.server.hostname=localhost" \
kafka-console-consumer \
  --bootstrap-server localhost:29092 \
  --topic monitor-test \
  --from-beginning
```

- Start graphical **JConsole**:

```bash
sudo jconsole
```

### Producer Metrics

- **response-rate**: average acknowledgements per seconds (global and per broker).
- **request-rate**: average requests per seconds (global and per broker).
- **request-latency-avg**: average request latency in miliseconds (only per broker).
- **outgoing-byte-rate**: bytes per seconds (global and per broker).
- **io-wait-time-ns-avg**: average time socket ready read/write in nanoseconds (only global).

### Consumer Metrics

- **records-lag-max**: maximum record lag. How far the consumer is behind producers.
- **bytes-consumed-rate**: rate of bytes consumed per second.
- **records-consumed-rate**: rate of records consumed per second.
- **fetch-rate**: fetch requests per second.

## Producer Tuning

- **acks**: Determines when the broker will acknowledge the record.

  - **0**: Producer will not wait for acknowledgements from ther server.
  - **1**: Record will be acknowledged when the leader writes the record to the disk.
  - **all / -1**: Record will be acknowledged only when the leader and all replicas have written the record to their disks.

- **retries**: Number of times to retry a record if there is a transient error. (**max.in.flight.requests.per.connection** to 1, to records not appear in different order).

- **batch.size**: Maximum number of bytes in a batch.

## Consumer Tuning:

- **fetch.min.bytes**: The minimum amount of data to fetch in a request.
- **heartbeat.interval. ms**: How often to send heartbeats to the consumer coordinator.

- **auto.offset.reset**: What to do when consumer has no initial offset.

  - **Latest**: Start at the latest record (default).
  - **Earliest**: Start at the earliest record.
  - **none**: Throw an exception when there is no existing offset data.

- **enable.auto.commit**: Periodically commit the current offset in the background.

## KSQL

- Configure KSQL Server:

```bash
# /etc/ksql/ksql-server.properties

bootstrap.servers=kafka-[BROKER_ID]:9092
ksql.streams.state.dir=/tmp/kafka-streams
```

- Enable KSQL service:

```bash
sudo systemctl enable confluent-ksql
```

- Start KSQL service:

```bash
sudo systemctl start confluent-ksql
```

- Create the topic _ksql-test_:

```bash
kafka-topics \
  --bootstrap-server localhost:29092 \
  --create \
  --topic ksql-test \
  --partitions 1 \
  --replication-factor 1
```

- Create a producer for _ksql-test_:

```bash
kafka-console-producer \
  --broker-list localhost:29092 \
  --topic ksql-test \
  --property parse.key=true \
  --property key.separator=:

# To add messages
>5:5,sarah,2
>7:7,andy,1
>5:5,sarah,3
```

- To start KSQL console:

```bash
sudo ksql http://localhost:28088
```

- To change a configuration:

```bash
SET 'auto.offset.reset' = 'earliest';
```

- List topics:

```bash
SHOW TOPICS;
```

- Show topic's data:

```bash
PRINT 'ksql-test' FROM BEGINNING;
```

- Create a stream from a topic:

```bash
CREATE STREAM ksql_test_stream (employee_id INTEGER, name VARCHAR, vacation_days INTEGER) WITH (kafka_topic='ksql-test', value_format='DELIMITED');
```

- Create a table from a topic:

```bash
CREATE TABLE ksql_test_table (employee_id INTEGER, name VARCHAR, vacation_days INTEGER) WITH (kafka_topic='ksql-test', value_format='DELIMITED', key='employee_id');
```

- Select from a stream or table:

```bash
SELECT * FROM ksql_test_stream EMIT CHANGES;
```

- Grouping from a stream:

```bash
SELECT SUM(vacation_days) FROM ksql_test_stream GROUP BY employee_id EMIT CHANGES;
```

- Conditional streaming:

```bash
CREATE STREAM member_signups
  (firstname VARCHAR,
    lastname VARCHAR,
    email_notifications BOOLEAN)
  WITH (KAFKA_TOPIC='member_signups',
    VALUE_FORMAT='DELIMITED');

CREATE STREAM member_signups_email AS
  SELECT * FROM member_signups WHERE email_notifications=true;
```

- Joining streamings:

```bash
CREATE STREAM member_signups
  (firstname VARCHAR,
    lastname VARCHAR)
  WITH (KAFKA_TOPIC='member_signups',
    VALUE_FORMAT='DELIMITED');

CREATE STREAM member_contact
  (email VARCHAR)
  WITH (KAFKA_TOPIC='member_contact',
    VALUE_FORMAT='DELIMITED');

CREATE STREAM member_email_list AS
  SELECT member_signups.firstname, member_signups.lastname, member_contact.email
  FROM member_signups
  INNER JOIN member_contact WITHIN 365 DAYS ON member_signups.rowkey = member_contact.rowkey;
```
