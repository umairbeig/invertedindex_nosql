package org.example;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.example.mapreduce.InvertedIndexMapper;
import org.example.mapreduce.InvertedIndexReducer;
public class Main extends Configured implements Tool {

    private static final String NAME = "Main";

    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        String[] otherArgs = new GenericOptionsParser(configuration, args).getRemainingArgs();
        int exitCode = ToolRunner.run(new Configuration(), new Main(), otherArgs);
        System.exit(exitCode);
    }

    @Override
    public int run(String[] args) {
        try {
            Job job = Job.getInstance(getConf(), "Inverted Index");

            job.setJarByClass(Main.class);
            job.addCacheFile(new Path(args[2]).toUri());
            job.setMapperClass(InvertedIndexMapper.class);
            job.setCombinerClass(InvertedIndexReducer.class);
            job.setReducerClass(InvertedIndexReducer.class);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(MapWritable.class);

            FileInputFormat.setInputPaths(job, new Path(args[0]));
            FileOutputFormat.setOutputPath(job, new Path(args[1]));

            return (job.waitForCompletion(true) ? 0 : 1);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Usage: " + NAME + " <input-directory> <table-name> <stop-words-file>");
            return 2;
        }
    }

}
