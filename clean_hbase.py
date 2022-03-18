#!/usr/bin/python3

# deletes every table in hbase
import happybase

# TODO: remove prints

HBASE_SERVER = 'localhost'
HBASE_PORT = 9090

# connect to HBase's thrift api
conn = happybase.Connection(HBASE_SERVER, HBASE_PORT)

for table in conn.tables():
    print("Deleting table ", table)
    conn.delete_table(table, True)