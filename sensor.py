#!/usr/bin/python3

# send random data from "sensors" to kafka

from time import sleep 
from time import time
from json import dumps
from random import gauss
from kafka import KafkaProducer
import sys

# TODO: change interval to 15 min
INTERVAL = 1
# TODO: send random non serial data
# TODO: increase number of sensors (run.sh and docker-compose.yml)

# sensor-id must be provided
# should be unique for every sensor
if len(sys.argv) != 2:
    print("Usage: ./sensor.py sensor-id")
    quit()

# get sensor-id
sensor_id = sys.argv[1]
print("Starting sensor {}".format(sensor_id))

# # create kafka producer
producer = KafkaProducer(
    bootstrap_servers = ['localhost:9092'],
    value_serializer = lambda x: dumps(x).encode('utf-8')
)

# continuously send data
while True:
    # random numerical data to send to kafka
    val = gauss(10,3)
    print("Sending value: {}".format(val))

    data = {'timestamp': time(), 'measurement': val}
    print(data)
    producer.send('sensor-{}'.format(sensor_id), value=data)
    sleep(INTERVAL)