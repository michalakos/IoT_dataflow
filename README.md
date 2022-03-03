# IoT_dataflow
Prototype for IoT system dataflow processing.

In this system, some sensors send virtual data in real time to a Kafka message broker, which are then processed with Apache Flink and stored in HBase. Then, the data are presented on Dashboards with the use of Grafana.

Technologies used:
* Apache Kafka
* Apache Flink
* HBase
* Grafana
