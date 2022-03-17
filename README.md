# IoT_dataflow
Prototype for IoT system dataflow processing.

In this system, some sensors send virtual data in real time to a Kafka message broker, which are then processed with Apache Flink and stored in HBase. Then, the data are presented on Dashboards with the use of Grafana.

Technologies used:
* Apache Kafka
* Apache Flink
* HBase
* Grafana

## Requirements
Linux packages:
* docker
* docker-compose

Python packages:
* kafka-python
* apache-flink

## Instructions
Execute **run.sh** to launch a kafka message broker via docker and create a topic for each of the sensors. To stop the sensors and the docker containers use ^c.