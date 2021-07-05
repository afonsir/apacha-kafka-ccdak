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

# Create a Producer and a Consumer

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
