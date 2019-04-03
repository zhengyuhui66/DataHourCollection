name := "DataHourCollection"

version := "0.1"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.apache.hadoop" % "hadoop-common" % "2.7.2",
  "org.apache.hadoop" % "hadoop-hdfs" % "2.7.2",
  "org.apache.hbase" % "hbase-common" % "1.1.1",
  "org.apache.hbase" % "hbase-client" % "1.1.1",
  "org.apache.hbase" % "hbase-server" % "1.1.1",
  "org.apache.spark" % "spark-core_2.11" % "2.1.1",
  "org.apache.spark" % "spark-streaming_2.11" % "2.1.1",
  "org.apache.spark" % "spark-streaming-kafka-0-10_2.11" % "2.1.1",
  "org.apache.kafka" % "kafka_2.11" % "0.10.1.0",
  "org.apache.kafka" % "kafka-streams" % "0.10.1.0",
  "org.apache.kafka" % "kafka-tools" % "0.10.1.0",
  "log4j" % "log4j" % "1.2.17"
)