#!/usr/bin/python3

# get aggragated data processed by flink from kafka
from kafka import KafkaConsumer
import happybase

# TODO: remove prints
KAFKA_BOOTSTRAP = 'localhost:9092'
HBASE_SERVER = 'localhost'
HBASE_PORT = 9090

late_consumer = KafkaConsumer(
    'late',
    bootstrap_servers = [KAFKA_BOOTSTRAP],
    auto_offset_reset = 'earliest',
    enable_auto_commit = True,
    value_deserializer = lambda x : x.decode("utf-8"),
    group_id = 'late_group',
)

# connect to HBase's thrift api
conn = happybase.Connection(HBASE_SERVER, HBASE_PORT)

# create column families for HBase
families = {
    'cf' : dict(),
}

conn.create_table('late_events', families)
table = conn.table('late_events')

# late events are represented by a key-value pair
# where key is timestamp
# and value is a column for sensor id and a column for value
for message in late_consumer:
    
    message = message.value
    parsed = message.split(',')
    sensor = parsed[0]
    time = parsed[1]
    value = parsed[2]

    print("Kafka data: sensor_id: ", sensor, " value: ", value, "\tat: ", time)

    row = ('cf:'+time).encode('utf-8')
    col1 = 'cf:sensor'.encode('utf-8')
    val1 = sensor.encode('utf-8')
    col2 = 'cf:value'.encode('utf-8')
    val2 = value.encode('utf-8')

    table.put(row, {col1:val1, col2:val2})