package com.github.tanacasino.dropsync

import com.github.tanacasino.dropsync.command.CommandException

object DropSyncMain {
  val Usage =
    """
      |Usage: java -jar dropsync.jar COMMAND [OPTIONS] [ARGS]
      |COMMAND
      |    sync
      |    setup (TODO)
      |    find (TODO)
      |    find-delete (TODO)
    """.stripMargin

  def main(args: Array[String]): Unit = {
    val startTime = System.currentTimeMillis

    val command = try {
      ArgumentParser.parseArgs(args)
    } catch {
      case _: IllegalArgumentException =>
        System.err.println(Usage)
        sys.exit(1)
    }

    val config = Configuration.load
    if (config.isEmpty) {
      System.err.println("Failed to load .dropsync.conf")
      System.err.println("You should run setup first. COMMAND: java -jar dropsync.jar setup")
      sys.exit(1)
    }

    val exitCode = try {
      command.execute(config)
    } catch {
      case e: CommandException =>
        System.err.println(e.getMessage)
        e.code
    }

    if (exitCode == 0) println(s"Completed total: ${(System.currentTimeMillis - startTime) / 1000} sec")
    sys.exit(exitCode)
  }
}
