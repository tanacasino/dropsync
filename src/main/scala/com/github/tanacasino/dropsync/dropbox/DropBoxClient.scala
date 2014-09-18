package com.github.tanacasino.dropsync.dropbox

import java.util.Locale

import com.dropbox.core.{DbxClient, DbxEntry, DbxRequestConfig}
import com.github.tanacasino.dropsync.{Entry, EntryClient}

import scala.collection.JavaConversions._


object DropBoxClient {

  val Version = "0.1"

  val UserAgent = s"DropSync/$Version"

  def apply(accessToken: String): DropBoxClient = {
    val config = new DbxRequestConfig(UserAgent, Locale.getDefault.toString)
    new DropBoxClient(new DbxClient(config, accessToken))
  }

}

class DropBoxClient(val client: DbxClient) extends EntryClient {

  private def listFiles(basePath: String, entry: DbxEntry): Stream[Entry] = listFiles(basePath, entry.path)

  def listFiles(basePath: String, path: String): Stream[Entry] = {
    client.getMetadataWithChildren(path) match {
      case null => Stream.empty
      case meta if meta.entry.isFile => Entry(basePath, meta.entry) #:: Stream.empty
      case meta =>
        val (dirs, files) = Option(meta.children).toStream.flatten.partition(_.isFolder)
        if(meta.entry.path == basePath)
          files.map(Entry.apply(basePath, _)) #::: dirs.flatMap(listFiles(basePath, _))
        else
          Entry(basePath, meta.entry) #:: files.map(Entry.apply(basePath, _)) #::: dirs.flatMap(listFiles(basePath, _))
    }
  }

}
