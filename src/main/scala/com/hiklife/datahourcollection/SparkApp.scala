package com.hiklife.datahourcollection

import java.text.SimpleDateFormat
import java.util.Calendar

import com.hiklife.utils.{CommUtil, HBaseUtil, LogUtil}
import org.apache.hadoop.hbase.{CellUtil, TableName}
import org.apache.hadoop.hbase.client.{ConnectionFactory, HTable, Put, Scan}
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.protobuf.ProtobufUtil
import org.apache.hadoop.hbase.util.{Base64, Bytes}
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.JavaConversions._

object SparkApp {
  def main(args: Array[String]): Unit = {
    val path = args(0)
    val configUtil = new ConfigUtil(path + "dataAnalysis/dataHourCollection.xml")
    var hBaseUtil = new HBaseUtil(path + "hbase/hbase-site.xml")
    var logUtil = new LogUtil(path + "log4j/log4j.properties")

    try{
      //创建表
      val tableName = configUtil.devHourTable
      hBaseUtil.createTable(tableName, "CF")

      val conf = new SparkConf().setAppName(configUtil.appName)
      val sc = new SparkContext(conf)

      var calendar = Calendar.getInstance()
      calendar.add(Calendar.HOUR, -1)
      val date = calendar.getTime()
      val formatter_hash = new SimpleDateFormat("yyyyMMdd")
      val formatter_rowkey = new SimpleDateFormat("yyyyMMddHH")

      val dir: String = path + "hbase/hbase-site.xml"
      val hconf = HBaseUtil.getConfiguration(dir)
      hconf.set(TableInputFormat.INPUT_TABLE, configUtil.devTotalTable)

      val dt = formatter_rowkey.format(date)
      val rowkey = Integer.toHexString(CommUtil.getHashCodeWithLimit(dt, 0xFFFE)).toUpperCase() + dt
      val scan = new Scan()
      scan.addFamily(Bytes.toBytes("CF"))
      scan.setStartRow(Bytes.toBytes(rowkey + "0000"))
      scan.setStopRow(Bytes.toBytes(rowkey + "9999"))
      val proto = ProtobufUtil.toScan(scan)
      val ScanToString = Base64.encodeBytes(proto.toByteArray)
      hconf.set(TableInputFormat.SCAN, ScanToString)

      val hBaseRDD = sc.newAPIHadoopRDD(hconf, classOf[TableInputFormat],
        classOf[org.apache.hadoop.hbase.io.ImmutableBytesWritable],
        classOf[org.apache.hadoop.hbase.client.Result])

      hBaseRDD.flatMap(x => x._2.listCells()).map(cell => {
        val dev = Bytes.toString(CellUtil.cloneQualifier(cell)).substring(14)
        val count = Bytes.toString(CellUtil.cloneValue(cell)).toLong
        (dev, count)
      }).reduceByKey(_ + _)
        .foreachPartition(partitionOfRecords => {
          val conn = ConnectionFactory.createConnection(HBaseUtil.getConfiguration(dir))
          val table = conn.getTable(TableName.valueOf(tableName)).asInstanceOf[HTable]
          table.setAutoFlush(false, false)
          table.setWriteBufferSize(5 * 1024 * 1024)
          partitionOfRecords.foreach(record => {
            val dt_hash = formatter_hash.format(date)
            val dt_rowkey = formatter_rowkey.format(date)
            val rowkey = Integer.toHexString(CommUtil.getHashCodeWithLimit(dt_hash, 0xFFFE)).toUpperCase() + dt_rowkey
            val put = new Put(rowkey.getBytes)
            put.addColumn("CF".getBytes, record._1.getBytes, String.valueOf(record._2).getBytes)
            table.put(put)
          })
          table.flushCommits()
          table.close()
          conn.close()
        })
    }catch {
      case e:Exception =>
        logUtil.error("设备采集统计（按小时）", e)
    }

  }
}
