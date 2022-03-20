package org.myorg.quickstart;

import org.apache.flink.api.common.eventtime.*;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.*;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple2;
import java.time.Duration;

// Web UI available at localhost:8081
// TODO: set interval

public class SensorJob {
    // interval is the seconds between each aggregation
    // the desired interval is one day
    final static int INTERVAL = 1*24*60*60;
    final static String bootstrap_server = "localhost:9092";

    public static void main(String[] args) throws Exception {


        // create kafka source by connecting to kafka
        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers(bootstrap_server)
                .setTopics("sensor_data")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        // create environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // create data stream from kafka source
        // values from source are of type:
        // sensor_id(String)|timestamp(Long)|value(Double)
        DataStream<String> stream = env.fromSource(
                source,
                WatermarkStrategy.noWatermarks(),
                "Kafka"
        );

        // kafka sinks receive a string of data created as:
        // sensor_id(String),value(Double)
        // create kafka sink for max values
        KafkaSink<String> maxSink = KafkaSink.<String>builder()
                .setBootstrapServers(bootstrap_server)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic("max")
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build())
                .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();

        // create kafka sink for min values
        KafkaSink<String> minSink = KafkaSink.<String>builder()
                .setBootstrapServers(bootstrap_server)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic("min")
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build())
                .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();

        // create kafka sink for sum of values
        KafkaSink<String> sumSink = KafkaSink.<String>builder()
                .setBootstrapServers(bootstrap_server)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic("sum")
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build())
                .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();

        // create kafka sink for average of values
        KafkaSink<String> avgSink = KafkaSink.<String>builder()
                .setBootstrapServers(bootstrap_server)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic("avg")
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build())
                .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();

        // create kafka sink for late events
        KafkaSink<String> lateSink = KafkaSink.<String>builder()
                .setBootstrapServers(bootstrap_server)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic("late")
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build())
                .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();


        // create output tag for late events
        final OutputTag<Tuple3<String, Long, Double>> lateOutputTag =
                new OutputTag<Tuple3<String, Long, Double>>("late-data"){};

        // detect late events
        SingleOutputStreamOperator<Tuple3<String, Long, Double>> getLateEvents = stream
                .flatMap(new Splitter())
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.
                                <Tuple3<String, Long, Double>>forBoundedOutOfOrderness(Duration.ofSeconds(0))
                                .withTimestampAssigner((event,timestamp)->event.f1*1000)
                )
                .keyBy(event->event.f0)
                .window(TumblingEventTimeWindows.of(Time.seconds(INTERVAL)))
                .allowedLateness(Time.seconds(0))
                .sideOutputLateData(lateOutputTag)
                .maxBy(2);

        // assign timestamps and watermark strategy (timestamp - second value of tuple)
        // assign key (sensor_id - first value of tuple)
        // create tumbling windows based on event time
        WindowedStream<Tuple3<String, Long, Double>, String, TimeWindow> initialStream = stream
                .flatMap(new Splitter())
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.
                                <Tuple3<String, Long, Double>>forBoundedOutOfOrderness(Duration.ofSeconds(0))
                                // timestamp by default is milliseconds, value in tuple is in seconds
                                // multiply by 1000 to align
                                .withTimestampAssigner((event,timestamp)->event.f1*1000)
                )
                .keyBy(event->event.f0)
                .window(TumblingEventTimeWindows.of(Time.seconds(INTERVAL)))
                .allowedLateness(Time.seconds(0));

        // get max value and push to sink
        initialStream
                .maxBy(2)
                .map((MapFunction<Tuple3<String, Long, Double>, String>)
                        value -> value.f0 + "," + value.f1 + "," + value.f2)
                .sinkTo(maxSink);

        // get min value and push to sink
        initialStream
                .minBy(2)
                .map((MapFunction<Tuple3<String, Long, Double>, String>)
                        value -> value.f0 + "," + value.f1 + "," + value.f2)
                .sinkTo(minSink);

        // get sum of values and push to sink
        initialStream
                .sum(2)
                .map((MapFunction<Tuple3<String, Long, Double>, String>)
                        value -> value.f0 + "," + (System.currentTimeMillis()/1000) + "," + value.f2)
                .sinkTo(sumSink);

        // get average of values and push to sink
        initialStream
                .aggregate(new AverageAggregate())
                .map((MapFunction<Tuple2<String, Double>, String>)
                        value -> value.f0  + "," + (System.currentTimeMillis()/1000) + "," + value.f1)//Tuple2::toString)
                .sinkTo(avgSink);

        // handle late events and push to sink
        getLateEvents
                .getSideOutput(lateOutputTag)
                .map((MapFunction<Tuple3<String, Long, Double>, String>)
                        value -> value.f0 + "," + value.f1 + "," + value.f2)
                .sinkTo(lateSink);

        env.execute();
    }

    // class to split strings into values
    public static class Splitter implements FlatMapFunction<String, Tuple3<String, Long, Double>> {
        @Override
        public void flatMap(String sentence, Collector<Tuple3<String, Long, Double>> out) {
            String[] parsed = sentence.split("\\|");

            String sensor = parsed[0];
            Long timestamp = Long.parseLong(parsed[1]);
            Double value = Double.parseDouble(parsed[2]);

            Tuple3<String, Long, Double> temp =
                    new Tuple3<>(sensor, timestamp, value);
            out.collect(temp);
        }
    }

    // class to average events
    public static class AverageAggregate
    implements AggregateFunction<Tuple3<String, Long, Double>, Tuple3<String, Long, Double>, Tuple2<String, Double>> {
        @Override
        public Tuple3<String, Long, Double> createAccumulator() {
            return new Tuple3<>("", 0L, 0.0);
        }

        @Override
        public Tuple3<String, Long, Double> add(Tuple3<String, Long, Double> value, Tuple3<String, Long, Double> accumulator) {
            return new Tuple3<>(value.f0, accumulator.f1 + 1L, accumulator.f2+value.f2);
        }

        @Override
        public Tuple2<String, Double> getResult(Tuple3<String,Long,Double> accumulator) {
            return new Tuple2<>(accumulator.f0, accumulator.f2/accumulator.f1);
        }

        @Override
        public Tuple3<String, Long, Double> merge(Tuple3<String,Long,Double>a, Tuple3<String,Long,Double>b) {
            return new Tuple3<>(a.f0, a.f1+b.f1, a.f2+b.f2);
        }
    }
}

