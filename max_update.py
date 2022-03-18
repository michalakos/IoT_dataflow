#!/usr/bin/python3

# get aggragated data processed by Flink from Kafka
# and store in HBase
from kafka import KafkaConsumer
import happybase
import sys

# TODO: remove prints

KAFKA_BOOTSTRAP = 'localhost:9092'
HBASE_SERVER = 'localhost'
HBASE_PORT = 9090

if len(sys.argv) != 2:
    print("Usage: ./max_update.py sensors_count")
    quit()

SENSOR_COUNT = sys.argv[1]
sensors = ["sensor_"+str(x+1) for x in range(int(SENSOR_COUNT))]

# create KafkaConsumer and connect to Kafka
max_consumer = KafkaConsumer(
    'max',
    bootstrap_servers = [KAFKA_BOOTSTRAP],
    auto_offset_reset = 'earliest',
    enable_auto_commit = True,
    value_deserializer = lambda x : x.decode("utf-8"),
    group_id = 'max_group',
)

# connect to HBase's thrift api
conn = happybase.Connection(HBASE_SERVER, HBASE_PORT)

# create column families for HBase
families = {
    'cf' : dict(),
}

sensors = ["sensor_"+str(x+1) for x in range(int(SENSOR_COUNT))]
tables = dict()

# create table to store max values for each sensor
# every row has the data's timestamp as a key and max_value as column
for sensor in sensors:
    conn.create_table(sensor+'_max_values', families)
    tables[sensor] = conn.table(sensor+'_max_values')

# wait for Kafka data and store in Hbase
for message in max_consumer:

    message = message.value
    parsed = message.split(',')
    sensor = parsed[0]
    time = parsed[1]
    value = parsed[2]

    table = tables[sensor]

    print("Kafka data: sensor_id: ", sensor, " value: ", value, "\tat: ", time)

    # convert strings to bytes to insert in HBase
    row = ('cf:'+time).encode('utf-8')
    col = 'cf:max_val'.encode('utf-8')
    val = value.encode('utf-8')

    table.put(row, {col:val})