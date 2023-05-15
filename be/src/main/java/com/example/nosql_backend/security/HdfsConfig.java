package com.example.nosql_backend.security;
import org.apache.hadoop.conf.Configuration;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class HdfsConfig {

    @Bean
    public Configuration hadoopConfiguration() {
        Configuration configuration = new Configuration();
        // Set HDFS configuration properties
        configuration.set("fs.defaultFS", "hdfs://localhost:9000");
        // Add more configuration properties if needed
        return configuration;
    }
}
