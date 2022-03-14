#!/usr/bin/python3

# send random data from "sensors" to kafka

from time import sleep, time
from json import dumps
from random import gauss, randint
from kafka import KafkaProducer
import sys

# TODO: change interval to 15 min
INTERVAL = 5
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

    # one in 30 data is previous day's
    timestamp = time()
    rint = randint(1,30)
    if rint == 1:
        timestamp -= 86400

    data = {'timestamp': timestamp, 'measurement': val}
    producer.send('sensor-{}'.format(sensor_id), value=data)
    sleep(INTERVAL)