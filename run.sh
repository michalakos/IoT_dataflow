#!/usr/bin/env bash

# script used to start kafka broker 
# and sensors

cd quickstart
mvn clean package

cleanup() {
    docker-compose down;
    kill 0;
    exit
}

cd ..

trap cleanup INT

docker-compose -f docker-compose.yml up -d
sleep 3
./sensor.py 1 &
./sensor.py 2 &

wait