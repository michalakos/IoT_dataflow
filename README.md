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
* Bash

## Requirements
* Python 3.9.7
* openjdk 1.8.0_312
* javac 1.8.0_312
* Docker 20.10.13
* docker-compose 1.29.2
* Apache Maven 3.6.3
* Apache Flink 1.14.3
* Apache HBase 2.4.10
* Grafana 8.4.4
* [Grafana JSON API](https://marcus.se.net/grafana-json-datasource/) 

Python packages:
* kafka-python
* happybase
* flask


## Instructions
* Change **HBASE_DIR** in `run.sh` to HBase installation path.
* Change **FLINK_DIR** in `run.sh` to Flink installation path.
* Execute `run.sh` from project's directory.
* Execute `sudo /bin/systemctl start grafana-server` on the command line to start Grafana's server.
* Go to `localhost:3000` on your browser.
* Select `Import`
* Select `Upload a JSON file`.
* Find and select `grafana-dashboard.json` from this project.
* On the last field select `sensor_data`.
* Import.
