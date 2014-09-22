package com.github.tanacasino.dropsync

import java.io.File


trait EntryClient {
  def listFiles(basePath: String, path: String): Stream[Entry]
}

trait RemoteEntryClient extends EntryClient {
  def upload(localEntry: Entry, remotePath: String): Unit
  def mkdir(path: String): Unit
}

class LocalFileSystemClient extends EntryClient {

  override def listFiles(basePath: String, path: String): Stream[Entry] = listFiles(basePath, new File(path))

  private def listFiles(basePath: String, f: File): Stream[Entry] = {
    if (basePath == f.getPath)
      Option(f.listFiles()).toStream.flatten.flatMap(listFiles(basePath, _))
    else
      Entry(basePath, f) #:: Option(f.listFiles()).toStream.flatten.flatMap(listFiles(basePath, _))
  }

}
