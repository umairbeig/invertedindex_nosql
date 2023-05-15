<!-- omit from toc -->
# Inverted Index Using MapReduce

An implementation of MapReduce to calculate the inverted index for any set of text documents.

Also comes with the full configuration required to set up a 3-node (1 master, 2 workers) Hadoop cluster and an utility script to write the output of the MapReduce job to a MongoDB database and collection.

<!-- omit from toc -->
## Table of Contents

- [Project Structure](#project-structure)
- [MapReduce](#mapreduce)
- [Python](#python)
- [Hadoop](#hadoop)

## Project Structure

```bash
.
├── .gitignore
├── hadoop
│   └── etc
│       └── hadoop
│           ├── core-site.xml
│           ├── hadoop-env.sh
│           ├── hdfs-site.xml
│           ├── mapred-site.xml
│           ├── workers
│           └── yarn-site.xml
├── pom.xml
├── python
│   ├── mongo.py
│   └── requirements.txt
└── src
    └── main
        ├── java
        │   └── org
        │       └── example
        │           ├── Main.java
        │           ├── mapreduce
        │           │   ├── InvertedIndexMapper.java
        │           │   └── InvertedIndexReducer.java
        │           └── util
        │               └── StringUtil.java
        └── resources
            └── META-INF
                └── MANIFEST.MF
```

- `src` and `pom.xml` - MapReduce job and their dependencies.
- `python` - Utility script to write data to MongoDB.
- `hadoop` - Configuration files for a 3-node Hadoop cluster.

## MapReduce

The MapReduce jobs are present in the `src` directory with their dependencies stated in `pom.xml`. The mapper and reducer are in two different classes for maximum extensibility. There are also some basic string utilities present in `StringUtil` such as functions for removing trailing and leading punctuation from tokens and to strip HTML tags from HTML content.

The jobs output files that look as shown below:

```tsv
Course  {f1.txt=10, f4.txt=14, f5.txt=16}
Court   {f3.txt=2, f4.txt=5, f2.txt=9}
```

There's are only two dependencies for the MapReduce jobs:

- Hadoop 3.3.5.
- JSoup 1.61.1.

The `pom.xml` file is set up so that `maven` always builds a FAT JAR with all the dependencies. The JAR will be named `inverted-index-1.0-SNAPSHOT.jar`. The JAR can be executed using`YARN` as follows:

```bash
yarn jar inverted-index-1.0-SNAPSHOT.jar <path-to-input> <path-to-output> <path-to-stop-words>
```

Where `<path-to-input>` is a directory on HDFS containing text files, `<path-to-output>` is a non-existent directory on HDFS where the output will be stored and `<path-to-stop-words>` is a the path to a file on HDFS which should be used to ignore certain words.

Example:

```bash
yarn jar inverted /input/Wikipedia /output/Wikipedia /input/stopwords.txt
```

## Python

The utility script that writes the output of the MapReduce jobs to MongoDB is present in the `python` directly, with its own `requirements.txt`. The script uses [`pymongo`](https://github.com/mongodb/mongo-python-driver) and [`joblib`](https://github.com/joblib/joblib) to write large amounts of data very quickly to MongoDB. It is implemented as a CLI application to allow for maximum customization. The usage is shown below:

```bash
$ python python/mongo.py --help
usage: mongo.py [-h] -c COLLECTION [-db DATABASE] [-pl PARALLELISM] [--host HOST] [-p PORT] input

Fast insert inverted index into MongoDB.

positional arguments:
  input                 inverted index file

options:
  -h, --help            show this help message and exit
  -c COLLECTION, --collection COLLECTION
                        mongo collection
  -db DATABASE, --database DATABASE
                        mongo db (default: test)
  -pl PARALLELISM, --parallelism PARALLELISM
                        number of jobs for joblib (default: 4)
  --host HOST           mongo host (default: localhost)
  -p PORT, --port PORT  mongo port (default: 27017)
```

The script writes the index for each word in the following format:

```json
{
    _id: ObjectId("6460f2afa8784601d0742d0e"),
    word: 'Course',
    docs: [
        {filename: 'f1.txt', count: 10},
        {filename: 'f4.txt', count: 14},
        {filename: 'f5.txt', count: 16}
    ]
}
```

## Hadoop

The Hadoop configuration files are present in the `hadoop` directory. The directory intentionally follows the directory structure that Hadoop has by default. This allows the user to directly copy the directory's files over the default files from Hadoop, using the command below, where `HADOOP_HOME` is the path to the `hadoop` installation on their system.

```bash
rsync -a hadoop/etc/hadoop/ ${HADOOP_HOME}/etc/hadoop
```

Some basic assumptions have been made about the machines:

- They are Linux-based virtual machines.
- Java 8 is installed.
- The master node is called `master` and the worker nodes are called `node1` and `node2` respectively
- The `master` is able to communicate with `node1` and `node2` via SSH.
- There is approximately 8GB of RAM and 4 virtual CPUs. The base values of various resources were based on 2GB machines and have been multiplied by 4 to obtain the values for 8GB machines. Users should down/upscale based on their requirements.

    The relevant files are [`mapred-site.xml`](./hadoop/etc/hadoop/mapred-site.xml) and [`yarn-site.xml`](./hadoop/etc/hadoop/yarn-site.xml).

The main things that have been configured:

- The main HDFS server runs on `master`, port `9000`.
- Only `node1` and `node2` run the `DataNode`.
- The data for the `NameNode` and `DataNodes` lives in `/home/hadoop/data/nameNode` and `/home/hadoop/data/dataNode`.
- Replication is set to 2 so that data is replicated in both `node1` and `node2`.
- The framework for MapReduce has been set as YARN.
- The `ApplicationMaster` is allowed to take up to 2GB's of RAM, and mappers and reducers are allowed up to 1GB of RAM.
- The amount of memory that can be allocated to YARN containers on a single node is set to 6GB.
- The maximum and minimum amount of memories that YARN containers can consume are set 6GB and 0.5GB.

Note:

The setting `yarn.resourcemanager.hostname` has been set to `127.0.0.1`  in [`yarn-site.xml`](./hadoop/etc/hadoop/yarn-site.xml) but should actually be the public IP address of the virtual machine. Users should make sure that this value is changed.

```xml
<property>
    <name>yarn.resourcemanager.hostname</name>
    <value>127.0.0.1</value>
</property>
```
