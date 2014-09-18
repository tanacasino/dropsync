package com.github.tanacasino.dropsync


object DropSync {
}

trait EntryClient {
  def listFiles(basePath: String, path: String): Stream[Entry]
}