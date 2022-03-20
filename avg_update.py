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
    print("Usage: ./avg_update.py sensors_count")
    quit()

SENSOR_COUNT = sys.argv[1]


avg_consumer = KafkaConsumer(
    'avg',
    bootstrap_servers = [KAFKA_BOOTSTRAP],
    auto_offset_reset = 'earliest',
    enable_auto_commit = True,
    value_deserializer = lambda x : x.decode("utf-8"),
    group_id = 'avg_group',
)

# connect to HBase's thrift api
conn = happybase.Connection(HBASE_SERVER, HBASE_PORT)

sensors = ["sensor_"+str(x+1) for x in range(int(SENSOR_COUNT))]
tables = dict()

# create table to store avg values for each sensor
# every row has the data's timestamp as a key and avg_value as column
for sensor in sensors:
    tables[sensor] = conn.table(sensor+'_avg_values')

# wait for Kafka data and store in Hbase
for message in avg_consumer:

    message = message.value
    parsed = message.split(',')
    sensor = parsed[0]
    time = parsed[1]
    value = parsed[2]

    table = tables[sensor]

    # convert strings to bytes to insert in HBase
    row = ('cf:'+time).encode('utf-8')
    col = 'cf:avg_val'.encode('utf-8')
    val = value.encode('utf-8')

    table.put(row, {col:val})