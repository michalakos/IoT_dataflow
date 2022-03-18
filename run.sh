#!/usr/bin/env bash

# script used to start kafka broker 
# and sensors

# change number of sensors
SENSORS=5

CUR_DIR=$PWD

# Replace with paths to hbase and flink 
HBASE_DIR=$HOME/Downloads/hbase-2.4.10
FLINK_DIR=$HOME/Downloads/flink-1.14.4

# shut down processes
cleanup() {
    echo "Shutting down docker..."
    cd $CUR_DIR
    docker-compose down --remove-orphans;

    echo "Stopping Flink cluster..."
    cd $FLINK_DIR
    ./bin/stop-cluster.sh

    echo "Stopping HBase..."
    cd $HBASE_DIR
    ./bin/stop-hbase.sh

    echo "Stopping sensors..."
    kill 0;

    exit
}

trap cleanup INT


echo "Starting HBase...\n"
cd $HBASE_DIR
./bin/start-hbase.sh &
sleep 3

echo "Starting HBase Thrift API...\n"
./bin/hbase thrift &


echo "Starting Flink cluster...\n"
cd $FLINK_DIR
./bin/start-cluster.sh &
sleep 3


echo "Creating job jar"
cd $CUR_DIR/quickstart
mvn clean package


echo "Starting Kafka...\n"
cd $CUR_DIR
docker-compose -f docker-compose.yml up -d
sleep 5

echo "Starting sensors...\n"
for i in $(seq 1 $SENSORS); do
    ./sensor.py $i &
done

echo "Submitting Flink job...\n"
cd $FLINK_DIR
./bin/flink run $CUR_DIR/quickstart/target/quickstart-0.1.jar &
sleep 5

echo "Starting database updates"
cd $CUR_DIR
./clean_hbase.py
./max_update.py $SENSORS &
./min_update.py $SENSORS &
./avg_update.py $SENSORS &
./sum_update.py $SENSORS &
./late_events.py &

wait