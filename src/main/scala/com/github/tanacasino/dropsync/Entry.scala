package com.github.tanacasino.dropsync

import java.io.File

import com.dropbox.core.DbxEntry


case class Entry(basePath: String, absPath: String, isFile: Boolean, stat: EntryStat) {
  def isDir: Boolean = !isFile
}

case class EntryStat(path: String, size: Long)

object Entry {
  def apply(basePath: String, entry: DbxEntry): Entry = {
    Entry(basePath, entry.path, entry.isFile, EntryStat(basePath, entry))
  }

  def apply(basePath: String, file: File): Entry = {
    Entry(basePath, file.getAbsolutePath, file.isFile, EntryStat(basePath, file))
  }
}

object EntryStat {
  def apply(basePath: String, entry: DbxEntry): EntryStat = {
    if (entry.isFile) {
      EntryStat(entry.path.stripPrefix(basePath), entry.asFile.numBytes)
    } else {
      EntryStat(entry.path.stripPrefix(basePath), 0)
    }
  }

  def apply(basePath: String, file: File): EntryStat = {
    if (file.isFile) {
      EntryStat(file.getAbsolutePath.stripPrefix(basePath), file.length)
    } else {
      EntryStat(file.getAbsolutePath.stripPrefix(basePath), 0)
    }
  }
}
