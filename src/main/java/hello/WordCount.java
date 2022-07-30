package hello;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;


import java.util.Properties;

public class WordCount {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers","192.168.88.128:9092");
        properties.setProperty("group.id","flink");
        FlinkKafkaConsumer<String> kafkaSource = new FlinkKafkaConsumer<String>("flink_kafka", new SimpleStringSchema(), properties);
        DataStream<String> kafkaDS = env.addSource(kafkaSource);
        DataStream<Tuple2<String, Integer>> dataStream=kafkaDS
                .flatMap(new Splitter())
                .keyBy(value -> value.f0)
                .sum(1);

        String filePath="D:/flink/src/main/data/sink";
        dataStream.writeAsText(filePath);


        dataStream.print();
        env.execute();
    }
    public static class Splitter implements FlatMapFunction<String, Tuple2<String, Integer>> {
        @Override
        public void flatMap(String sentence, Collector<Tuple2<String, Integer>> out) throws Exception {
            for (String word: sentence.split(" ")) {
                out.collect(new Tuple2<String, Integer>(word, 1));
            }
        }
    }
}