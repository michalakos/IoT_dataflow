#!/usr/bin/python3

# send random data from "sensors" to kafka

from cmath import log
from encodings import utf_8
from time import sleep, time
from random import gauss, randint
from kafka import KafkaProducer
import sys

# interval is the amount of seconds between each one of 
# the sensor's readings 
# the desired interval is 15 minutes
INTERVAL = 15*60

# sensor-id must be provided
# should be unique for every sensor
if len(sys.argv) != 2:
    print("Usage: ./sensor.py sensor-id")
    quit()

# time_of_start = time()

# get sensor-id
sensor_id = sys.argv[1]
print("Starting sensor-{}".format(sensor_id))

# create kafka producer
producer = KafkaProducer(
    bootstrap_servers = ['localhost:9092'],
    value_serializer = str.encode
)

late = False

# continuously send data
while True:
    # random numerical data to send to kafka
    val = gauss(10,3)

    # current time as timestamp
    timestamp = time()

    # one in 30 data is previous day's
    rint = randint(1,30)
    if rint == 1:
        late = True
        timestamp -= 86400
    else:
        late = False

    # send data of type 'sensor_{sensor_id}|timestamp|value' from each sensor
    data = 'sensor_{}|{}|{}'.format(sensor_id, int(timestamp), val)

    producer.send('sensor_data', value=data)
    sleep(INTERVAL)