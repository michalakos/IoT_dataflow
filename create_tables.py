#!/usr/bin/python3

# deletes every table in hbase
import happybase
import sys

HBASE_SERVER = 'localhost'
HBASE_PORT = 9090

if len(sys.argv) != 2:
    print("Usage: ./create_tables.py sensors_count")
    quit()


# connect to HBase's thrift api
conn = happybase.Connection(HBASE_SERVER, HBASE_PORT)


SENSOR_COUNT = sys.argv[1]

# create column families for HBase
families = {
    'cf' : dict(),
}

sensors = ["sensor_"+str(x+1) for x in range(int(SENSOR_COUNT))]

# create table to store avg values for each sensor
# every row has the data's timestamp as a key and avg_value as column
for sensor in sensors:
    print("Creating tables for",sensors)
    conn.create_table(sensor+'_max_values', families)
    conn.create_table(sensor+'_min_values', families)
    conn.create_table(sensor+'_avg_values', families)
    conn.create_table(sensor+'_sum_values', families)

print("Creating table for late events")
conn.create_table('late_events', families)