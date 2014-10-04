package com.github.tanacasino.dropsync.command

import com.typesafe.config.Config

trait SubCommand {

  def execute(implicit conf: Config): Int

}