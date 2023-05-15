package org.example.mapreduce;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class InvertedIndexReducer
        extends Reducer<Text, MapWritable, Text, MapWritable> {

    private final MapWritable result = new MapWritable();

    @Override
    public void reduce(Text key, Iterable<MapWritable> values,
                       Context context
    ) throws IOException, InterruptedException {
        Map<String, Integer> docCount = new HashMap<>();

        for (MapWritable val : values) {
            for (Map.Entry<Writable, Writable> entry : val.entrySet()) {
                String docID = entry.getKey().toString();

                int count = ((IntWritable) entry.getValue()).get();

                if (docCount.containsKey(docID)) {
                    docCount.put(docID, docCount.get(docID) + count);
                } else {
                    docCount.put(docID, count);
                }
            }
        }

        for (Map.Entry<String, Integer> entry : docCount.entrySet()) {
            result.put(new Text(entry.getKey()), new IntWritable(entry.getValue()));
        }

        context.write(key, result);
    }
}