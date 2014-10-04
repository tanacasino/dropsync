package com.github.tanacasino.dropsync

import com.github.tanacasino.dropsync.command.{SubCommand, SyncCommand}


object ArgumentParser {

  def parseArgs(args: Array[String]): SubCommand = {
    args.toList match {
      case "sync" :: local :: remote :: Nil => SyncCommand(local, remote)
      case _ => throw new IllegalArgumentException("Illegal Argument")
    }
  }

}
