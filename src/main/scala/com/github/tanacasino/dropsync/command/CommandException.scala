package com.github.tanacasino.dropsync.command

case class CommandException(message: String, code: Int) extends Exception(message)
