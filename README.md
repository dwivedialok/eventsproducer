# Useful Commands
We recommend doing **all the hands-on work** including registering for Instaclustr account **from Ubuntu VM image** that was provided to you. If you still don’t have this image, then ask and we will provide the image ova file to you. Opening Instaclustr platform console website in VM makes it easy to copy connection credentials when connecting to C* or Kafka clusters.

## Using Instaclustr Managed C* cluster
- Connect to Cluster by using cqlsh

From within cqlsh run following commands to create keysapce and table and then insert/select records
- Create Keyspace
- Create Table
- Insert records
- Select records


## Using Instaclustr Managed Kafka Cluster
- Create kafka.properties file
```bash
cd ~/Downloads/kafka_client/bin/
wget  https://raw.githubusercontent.com/dwivedialok/eventsproducer/master/kafka.properties
```
Now open it in an editor e.g. nano using command below
```bash
nano kafka.properties
```
You will see text like one below, where you just need to change the password for your kafka cluster. You will get password from **Connection Info** section of your Instaclustr managed Kafka cluster. If for any reason you cannot download this file using above commands, then you can use text below or get it from ‘Connection Info’ section of your Instaclustr managed Kafka cluster.
Save kafka.properties file in `~/Downloads/kafka_client/bin/`
```text
security.protocol=SASL_PLAINTEXT
sasl.mechanism=SCRAM-SHA-256 sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required \
    username="ickafka" \
    password="[USER PASSWORD]";
```

- Connect to cluster and list topics
```bash
./ic-kafka-topics.sh --bootstrap-server <cluster public IP>:9092 --properties-file kafka.properties --list
```
- Create new topic
```bash
./ic-kafka-topics.sh --bootstrap-server <cluster public IP>:9092 --properties-file kafka.properties --create --topic events --replication-factor 3 --partitions 3
```
- Start Console consumer

## Developing Java based Producer and Consumers

You can get the code repository locally by using following commands

```bash
 mkdir -p ~/dev/training
 cd ~/dev/training/
 git clone https://github.com/dwivedialok/eventsproducer.git
 git clone https://github.com/dwivedialok/eventsconsumer.git
```

Now you can import these projects into IntelliJ using pom.xml file and using “Import project” option of IntelliJ. However, we suggest creating project step by step as will be shown in the hands-on exercise session in the workshop. 
