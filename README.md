# IoT_dataflow
Prototype for IoT system dataflow processing.

In this system, some sensors send virtual data in real time to a Kafka message broker, which are then processed with Apache Flink and stored in HBase. Then, the data are presented on Dashboards with the use of Grafana.

Technologies used:
* Apache Kafka
* Apache Flink
* HBase
* Grafana

Languages:
* Python
* Java

## Requirements
Linux packages:
* python3
* docker
* docker-compose
* Apache Maven 3.6.3
* Apache Flink 1.14.3

Python packages:
* kafka-python


## Instructions
* Execute **run.sh** to launch a kafka message broker via docker and create a topic for each of the sensors. To stop the sensors and the docker containers use ^c.<br>
* Start a flink cluster by executing bin/start-cluster.sh from flink's directory.
* Submit the job's jar file as such:
`bin/flink run path/to/job/jar`
