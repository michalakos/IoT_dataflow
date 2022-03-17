/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//
//package org.myorg.quickstart;
//
//import org.apache.flink.api.common.serialization.SimpleStringSchema;
//import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
//
///**
// * Skeleton for a Flink Streaming Job.
// *
// * <p>For a tutorial how to write a Flink streaming application, check the
// * tutorials and examples on the <a href="https://flink.apache.org/docs/stable/">Flink Website</a>.
// *
// * <p>To package your application into a JAR file for execution, run
// * 'mvn clean package' on the command line.
// *
// * <p>If you change the name of the main class (with the public static void main(String[] args))
// * method, change the respective entry in the POM.xml file (simply search for 'mainClass').
// */
//
//import org.apache.flink.api.common.functions.FlatMapFunction;
//import org.apache.flink.api.java.tuple.Tuple2;
//import org.apache.flink.streaming.api.datastream.DataStream;
//import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
//import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
//import org.apache.flink.streaming.api.windowing.time.Time;
//import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
//import org.apache.flink.util.Collector;
//
//import java.util.Properties;
//
//public class StreamingJob {
//
//		public static void main(String[] args) throws Exception {
//
//			StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//
//			Properties properties = new Properties();
//			properties.setProperty("bootstrap-servers", "localhost:9092");
//
//			FlinkKafkaConsumer011<String> flinkKafkaConsumer = new FlinkKafkaConsumer011<>(
//					"sensor-1",
//					new SimpleStringSchema(),
//					properties
//			);
//
//			DataStream<Tuple2<String, Integer>> dataStream = env
//					.addSource(flinkKafkaConsumer)
//					.flatMap(new Splitter())
//					.keyBy(value -> value.f0)
//					.window(TumblingProcessingTimeWindows.of(Time.seconds(5)))
//					.sum(1);
//
//			dataStream.print();
//
//			env.execute("Window WordCount");
//		}
//
//		public static class Splitter implements FlatMapFunction<String, Tuple2<String, Integer>> {
//			@Override
//			public void flatMap(String sentence, Collector<Tuple2<String, Integer>> out) throws Exception {
//				for (String word: sentence.split(" ")) {
//					out.collect(new Tuple2<String, Integer>(word, 1));
//				}
//			}
//		}
//
//	}