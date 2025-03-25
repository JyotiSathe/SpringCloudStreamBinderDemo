### **NATS Commands**

Publish
```
PS C:\Users\User> docker run --network host -it natsio/nats-box
nats-box v0.16.0
host:~# `nats publish train.updates "hello"`
08:26:08 Published 5 bytes to "train.updates"
host:~#
```

Subscribe
```
PS C:\Users\User> docker run --network host -it natsio/nats-box
nats-box v0.16.0
host:~# `nats sub "train.updates"`
08:25:32 Subscribing on train.updates
[#1] Received on "train.updates"
hello
```

### **Kafka Commands**
```
docker ps | findstr kafka
docker exec -it <container-id> bash
```
#### `List topics`
kafka-topics --bootstrap-server localhost:9092 --list

#### `Describe a topic (shows partition information)`
kafka-topics --bootstrap-server localhost:9092 --describe --topic topicA

#### `List consumer groups`
kafka-consumer-groups --bootstrap-server localhost:9092 --list

#### `Describe consumer group`
kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group train-consumer-group

#### `Read messages with partition information`
kafka-console-consumer --bootstrap-server localhost:9092 \
--topic topicA \
--from-beginning \
--property print.partition=true \
--property print.offset=true \
--property print.key=true
