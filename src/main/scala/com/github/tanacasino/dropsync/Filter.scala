package com.github.tanacasino.dropsync

import java.io.File

object Filter {
  val IgnorePathNames = List(".DS_Store", "Thumbs.db", ".dropbox")
  val IgnorePathPrefixNames = List("._")

  def defaultHandler = new FilterHandler {
    lazy val filters = {
      IgnorePathNames.map(new PathNameMatchFilter(_)) :::
      IgnorePathPrefixNames.map(new PathNamePrefixFilter(_))
    }
    override def skip(path: String): Boolean = {
      filters.exists(_.skip(path))
    }
  }
}

trait FilterHandler {
  def skip(path: String): Boolean
}

trait Filter {
  def skip(path: String): Boolean
}

class PathNameMatchFilter(val filterPath: String) extends Filter {
  override def skip(path: String): Boolean = {
    val file = new File(path)
    file.getName == filterPath
  }
}

class PathNamePrefixFilter(val filterPath: String) extends Filter {
  override def skip(path: String): Boolean = {
    val file = new File(path)
    file.getName.startsWith(filterPath)
  }
}
