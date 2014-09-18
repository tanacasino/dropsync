package com.github.tanacasino.dropsync.local

import java.io.File

import com.github.tanacasino.dropsync.{Entry, EntryClient}

class LocalFileSystemClient extends EntryClient {

  def listFiles(basePath: String, path: String): Stream[Entry] = listFiles(basePath, new File(path))

  private def listFiles(basePath: String, f: File): Stream[Entry] = {
    if(basePath == f.getPath)
      Option(f.listFiles()).toStream.flatten.flatMap(listFiles(basePath, _))
    else
      Entry(basePath, f) #:: Option(f.listFiles()).toStream.flatten.flatMap(listFiles(basePath, _))
  }

}
