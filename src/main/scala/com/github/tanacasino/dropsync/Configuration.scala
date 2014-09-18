package com.github.tanacasino.dropsync

import java.io.File

import com.typesafe.config.{Config, ConfigFactory}

import scala.util.Properties

object Configuration {

  val ConfigFileName = ".dropsync.conf"

  def load(): Option[Config] = {
    val baseDir = Properties.envOrElse("HOME", "")
    val file = new File(baseDir + File.separator + ConfigFileName)
    if (file.isFile) {
      Option(ConfigFactory.parseFile(file))
    } else {
      None
    }
  }

}
