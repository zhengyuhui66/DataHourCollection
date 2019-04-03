package com.hiklife.datahourcollection

import org.apache.commons.configuration.XMLConfiguration

class ConfigUtil(path: String) extends Serializable {
  val conf = new XMLConfiguration(path)

  def getConfigSetting(key: String, default: String): String ={
    if(conf != null)
      conf.getString(key)
    else
      default
  }

  /*
  spark应用名称
   */
  val appName: String = getConfigSetting("appName", "DataHourCollection")

  /*
  设备采集实时统计表
   */
  val devTotalTable: String = getConfigSetting("devTotalTable", "")

  /*
  设备采集统计表（按小时）
   */
  val devHourTable: String = getConfigSetting("devHourTable", "")

}
