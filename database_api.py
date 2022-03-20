#!/usr/bin/python3

# get data from HBase
import happybase
from datetime import datetime


# connect to HBase's thrift api
def connect_to_database():
    HBASE_SERVER = 'localhost'
    HBASE_PORT = 9090
    conn = happybase.Connection(HBASE_SERVER, HBASE_PORT)
    return conn


# get all max values for a specific sensor
# sensor_id: number of sensor (typically unsigned integer)
# returns json object of type:
# {"sensor-id":sensor-id, 
# "time":datetime,
# "value":value}
def get_max(sensor_id):
    conn = connect_to_database()

    data = []
    try:
        table = conn.table('sensor_'+str(sensor_id)+'_max_values')
        
        for key, val in table.scan():

            key = key.decode('utf-8').replace('cf:','')
            value = val[b'cf:max_val'].decode('utf-8')

            data.append(
                {
                "sensor-id":"sensor-"+str(sensor_id),
                "time":str(datetime.fromtimestamp(int(key))),
                "value":value
                })
    except Exception as e: 
        print(e)
        data = [{}]

    return(data)


# get all min values for a specific sensor
# returns list of json objects of type:
# {"sensor-id":sensor-id, 
# "time":datetime,
# "value":value}
def get_min(sensor_id):
    conn = connect_to_database()

    data = []
    try:
        table = conn.table('sensor_'+str(sensor_id)+'_min_values')
        
        for key, val in table.scan():

            key = key.decode('utf-8').replace('cf:','')
            value = val[b'cf:min_val'].decode('utf-8')

            data.append(
                {
                "sensor-id":"sensor-"+str(sensor_id),
                "time":str(datetime.fromtimestamp(int(key))),
                "value":value
                })
    except Exception as e: 
        print(e)
        data = [{}]

    return(data)


# get all avg values for a specific sensor
# returns list of json objects of type:
# {"sensor-id":sensor-id, 
# "time":datetime,
# "value":value}
def get_avg(sensor_id):
    conn = connect_to_database()

    data = []
    try:
        table = conn.table('sensor_'+str(sensor_id)+'_avg_values')
        
        for key, val in table.scan():

            key = key.decode('utf-8').replace('cf:','')
            value = val[b'cf:avg_val'].decode('utf-8')

            data.append(
                {
                "sensor-id":"sensor-"+str(sensor_id),
                "time":str(datetime.fromtimestamp(int(key))),
                "value":value
                })
    except Exception as e: 
        print(e)
        data = [{}]

    return(data)


# get all sum values for a specific sensor
# returns list of json objects of type:
# {"sensor-id":sensor-id, 
# "time":datetime,
# "value":value}
def get_sum(sensor_id):
    conn = connect_to_database()

    data = []
    try:
        table = conn.table('sensor_'+str(sensor_id)+'_sum_values')
        
        for key, val in table.scan():

            key = key.decode('utf-8').replace('cf:','')
            value = val[b'cf:sum_val'].decode('utf-8')

            data.append(
                {
                "sensor-id":"sensor-"+str(sensor_id),
                "time":str(datetime.fromtimestamp(int(key))),
                "value":value
                })
    except Exception as e: 
        print(e)
        data = [{}]

    return(data)


# get all late events
# returns list of json objects of type:
# {"sensor-id":sensor-id, 
# "time":datetime,
# "value":value}
def get_late():
    conn = connect_to_database()

    data = []
    try:
        table = conn.table('late_events')
        
        for key, val in table.scan():

            key = key.decode('utf-8').replace('cf:','')
            sensor = val[b'cf:sensor'].decode('utf-8')
            value = val[b'cf:value'].decode('utf-8')

            data.append(
                {
                "sensor-id":sensor,
                "time":str(datetime.fromtimestamp(int(key))),
                "value":value
                })
    except Exception as e: 
        print(e)
        data = [{}]

    return(data)