#!/usr/bin/python3

# get aggragated data processed by Flink from Kafka
# and store in HBase
from kafka import KafkaConsumer
import happybase
import sys

KAFKA_BOOTSTRAP = 'localhost:9092'
HBASE_SERVER = 'localhost'
HBASE_PORT = 9090

if len(sys.argv) != 2:
    print("Usage: ./sum_update.py sensors_count")
    quit()

SENSOR_COUNT = sys.argv[1]

# create KafkaConsumer and connect to Kafka
sum_consumer = KafkaConsumer(
    'sum',
    bootstrap_servers = [KAFKA_BOOTSTRAP],
    auto_offset_reset = 'earliest',
    enable_auto_commit = True,
    value_deserializer = lambda x : x.decode("utf-8"),
    group_id = 'sum_group',
)

# connect to HBase's thrift api
conn = happybase.Connection(HBASE_SERVER, HBASE_PORT)

sensors = ["sensor_"+str(x+1) for x in range(int(SENSOR_COUNT))]
tables = dict()

# create table to store sum values for each sensor
# every row has the data's timestamp as a key and sum_value as column
for sensor in sensors:
    tables[sensor] = conn.table(sensor+'_sum_values')

# wait for Kafka data and store in Hbase
for message in sum_consumer:

    message = message.value
    parsed = message.split(',')
    sensor = parsed[0]
    time = parsed[1]
    value = parsed[2]

    table = tables[sensor]

    # convert strings to bytes to insert in HBase
    row = ('cf:'+time).encode('utf-8')
    col = 'cf:sum_val'.encode('utf-8')
    val = value.encode('utf-8')

    table.put(row, {col:val})