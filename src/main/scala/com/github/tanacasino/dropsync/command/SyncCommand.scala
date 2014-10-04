package com.github.tanacasino.dropsync.command

import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

import com.github.tanacasino.dropsync.dropbox.DropBoxClient
import com.github.tanacasino.dropsync.{Entry, Filter, LocalFileSystemClient}
import com.typesafe.config.Config

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}


case class SyncCommand(local: String, remote: String) extends SubCommand {
  override def execute(implicit config: Config): Int = {
    if (!new File(local).exists) {
      throw new CommandException(s"File or directory does not exists: $local", 1)
    }
    println(s"local  path : $local")
    println(s"remote path : $remote")
    println("Fetching remote files metadata")
    val fetchStart = System.currentTimeMillis
    val accessToken = config.getString("accessToken")
    val client = DropBoxClient(accessToken)
    val remoteEntries = client.listFiles(remote, remote)
    println("remote entries size : " + remoteEntries.size)
    println(s"Fetched. time=${(System.currentTimeMillis - fetchStart) / 1000} sec")

    val localClient = new LocalFileSystemClient
    val localEntries = localClient.listFiles(local, local)
    println("local entries size : " + localEntries.size)

    val diff = localEntries.filter { local =>
      !remoteEntries.exists(_.stat == local.stat)
    }.sortWith { (e1, e2) =>
      e1.stat.path.compareTo(e2.stat.path) < 0
    }
    println("diff entries size: " + diff.size)

    val (dirs, files) = diff.partition(_.isDir)
    dirs.foreach { dir =>
      println(s"D : l:${dir.absPath}, r:${remote + dir.stat.path}")
    }

    val filter = Filter.defaultHandler
    val (ignores, uploads) = files.partition { f =>
      filter.skip(f.stat.path)
    }
    uploads.foreach { file =>
      println(s"F : l:${file.absPath}, r:${remote + file.stat.path}, size:${file.stat.size}")
    }
    println("Ignores : ")
    ignores.foreach { ignore =>
      println(s"I : ${ignore.absPath}, size:${ignore.stat.size}")
    }
    println("")

    // Make directories (serial execution)
    dirs.foreach { dir =>
      println(s"making : r:${remote + dir.stat.path}, l:${dir.absPath}")
      client.mkdir(remote + dir.stat.path)
    }

    // Upload files (parallel execution)
    val pool = Executors.newFixedThreadPool(5) // TODO Should be configurable
    implicit val context = ExecutionContext.fromExecutorService(pool)
    val success = new AtomicLong
    val failure = new AtomicLong

    val futures: Seq[Future[Entry]] = for (i <- 0 to uploads.size - 1) yield {
      val upload = uploads(i)
      val f: Future[Entry] = Future {
        val start = System.currentTimeMillis
        println(s"uploading $i: r:${remote + upload.stat.path}, l:${upload.absPath}, size:${upload.stat.size}")
        client.upload(upload, remote)
        val uploadTime = (System.currentTimeMillis - start) / 1000
        println(s"uploaded  $i: r:${remote + upload.stat.path}, l:${upload.absPath}, size:${upload.stat.size}, uploadTime:$uploadTime")
        upload
      }
      f.onSuccess {
        case entry: Entry =>
          println(s"$i : onSuccess : $upload")
          success.incrementAndGet()
      }
      f.onFailure {
        case t: Throwable =>
          println(s"$i : onFailure : file:$upload, msg:${t.getMessage}")
          failure.incrementAndGet()
      }
      f
    }

    val result = Await.ready(Future.sequence(futures), Duration.Inf)
    result.foreach { r =>
      ()
    }
    context.shutdown

    println(s"success : ${success.get}")
    println(s"failure : ${failure.get}")
    val bytes = uploads.map(_.stat.size).foldLeft(0L)(_ + _)
    println(s"Uploaded total size : ${bytes / 1024 / 1024} MB")

    0
  }
}