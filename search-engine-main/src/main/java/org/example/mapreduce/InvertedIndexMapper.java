package org.example.mapreduce;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.example.util.StringUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

public class InvertedIndexMapper
        extends Mapper<Object, Text, Text, MapWritable> {

    private final Text word = new Text();
    private final MapWritable docMap = new MapWritable();

    private final Set<String> stopWords = new HashSet<>();
    private String fileName = "";

    private final String PUNCTUATION = " !\"',;:.-_?)([]<>*#\n\t\r";

    @Override
    public void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);
        // Set the filename so that it can be used as the document ID
        fileName = ((FileSplit) context.getInputSplit()).getPath().getName();

        // Read the stop words file and store the words in a HashSet
        URI stopWordsFile = context.getCacheFiles()[0];

        FileSystem fs = FileSystem.get(context.getConfiguration());
        Path filepath = new Path(stopWordsFile.toString());

        BufferedReader reader = new BufferedReader(new InputStreamReader(fs.open(filepath)));

        String line;

        while ((line = reader.readLine()) != null) {
            StringTokenizer itr = new StringTokenizer(line);

            while (itr.hasMoreTokens()) {
                stopWords.add(itr.nextToken());
            }
        }

        reader.close();

    }

    @Override
    public void map(Object key, Text value, Context context
    ) throws IOException, InterruptedException {
        Text docMapKey = new Text(fileName);

        String text = StringUtil.htmlSanitize(value.toString());
        StringTokenizer itr = new StringTokenizer(text);

        while (itr.hasMoreTokens()) {
            String token = itr.nextToken();
            token = StringUtil.strip(token, PUNCTUATION);

            if (!stopWords.contains(token)) {
                word.set(token);

                if (docMap.containsKey(new Text(docMapKey))) {
                    ((IntWritable) docMap.get(new Text(docMapKey))).set(((IntWritable) docMap.get(new Text(docMapKey))).get() + 1);
                } else {
                    docMap.put(new Text(docMapKey), new IntWritable(1));
                }

                context.write(word, docMap);
            }
        }
    }
}