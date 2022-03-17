package org.myorg.quickstart;

import org.apache.flink.api.common.eventtime.*;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
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
import java.util.Properties;

public class SensorJob {
    final static int INTERVAL = 5;

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", "localhost:9092");
        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers("localhost:9092")
                .setTopics("sensor_data")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStream<String> stream = env.fromSource(
                source,
                WatermarkStrategy.noWatermarks(),
                "Kafka"
        );

        final OutputTag<Tuple3<String, Long, Double>> lateOutputTag =
                new OutputTag<Tuple3<String, Long, Double>>("late-data"){};

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

        WindowedStream<Tuple3<String, Long, Double>, String, TimeWindow> initialStream = stream
                .flatMap(new Splitter())
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.
                                <Tuple3<String, Long, Double>>forBoundedOutOfOrderness(Duration.ofSeconds(0))
                                .withTimestampAssigner((event,timestamp)->event.f1*1000)
                )
                .keyBy(event->event.f0)
                .window(TumblingEventTimeWindows.of(Time.seconds(INTERVAL)))
                .allowedLateness(Time.seconds(0));

        DataStreamSink<String> maxStream = initialStream
                .maxBy(2)
                .map((MapFunction<Tuple3<String, Long, Double>, String>)
                        value -> "Max: "+value)
                .print();

        DataStreamSink<String> minStream = initialStream
                .minBy(2)
                .map((MapFunction<Tuple3<String, Long, Double>, String>)
                        value -> "Min: "+value)
                .print();

        DataStreamSink<String> sumStream = initialStream
                .sum(2)
                .map((MapFunction<Tuple3<String, Long, Double>, String>)
                        value -> "Sum: "+value)
                .print();

        DataStreamSink<String> avgStream = initialStream
                .aggregate(new AverageAggregate())
                .map((MapFunction<Tuple2<String, Double>, String>) value -> "Average:"+value)
                .print();

        DataStreamSink<String> lateStream = getLateEvents
                .getSideOutput(lateOutputTag)
                        .map((MapFunction<Tuple3<String, Long, Double>, String>)
                                value -> "Late event: "+value)
                                .print();

        env.execute();
    }

    public static class Splitter implements FlatMapFunction<String, Tuple3<String, Long, Double>> {
        @Override
        public void flatMap(String sentence, Collector<Tuple3<String, Long, Double>> out) throws Exception {
            String[] parsed = sentence.split("\\|");

            String sensor = parsed[0];
            Long timestamp = Long.parseLong(parsed[1]);
            Double value = Double.parseDouble(parsed[2]);

            Tuple3<String, Long, Double> temp =
                    new Tuple3<>(sensor, timestamp, value);
            out.collect(temp);
        }
    }

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

