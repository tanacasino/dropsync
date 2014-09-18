package com.github.tanacasino.dropsync

import java.io.File
import java.util.concurrent.Executors

import com.github.tanacasino.dropsync.dropbox.DropBoxClient
import com.github.tanacasino.dropsync.local.LocalFileSystemClient

import scala.concurrent.duration.Duration

import scala.concurrent.{Await, ExecutionContext, Future}


object Main {
  val Usage =
    """
      |Usage: java -jar dropsync.jar COMMAND [OPTIONS] [ARGS]
      |COMMAND
      |    setup
      |    sync
      |    find
      |    find-delete
    """.stripMargin

  def main(args: Array[String]): Unit = {
    val startTime = System.currentTimeMillis
    // TODO Use library for arguments parser
    if (args.size < 1) {
      System.err.println(Usage)
      sys.exit(1)
    }
    if (args(0) != "sync") {
      System.err.println(Usage)
      sys.exit(1)
    }
    val localPath = args(1)
    val remotePath = args(2)

    // Check local path exists?
    if (!new File(localPath).exists) {
      System.err.println(s"File does not exist: $localPath")
      sys.exit(1)
    }
    // TODO remote file exists ?

    val config = Configuration.load
    if (config.isEmpty) {
      System.err.println("Failed to load .dropsync.conf")
      System.err.println("You should run setup first. COMMAND: java -jar dropsync.jar setup")
      sys.exit(1)
    }

    println(s"local  path : $localPath")
    println(s"remote path : $remotePath")
    println("Fetching remote files metadata")
    val fetchStart = System.currentTimeMillis
    val accessToken = config.get.getString("accessToken")
    val client = DropBoxClient(accessToken)
    val remoteEntries = client.listFiles(remotePath, remotePath)
    println("remote entries size : " + remoteEntries.size)
    println(s"Fetched. time=${(System.currentTimeMillis - fetchStart) / 1000} sec")

    val localClient = new LocalFileSystemClient
    val localEntries = localClient.listFiles(localPath, localPath)
    println("local entries size : " + localEntries.size)

    val diff = localEntries.filter { local =>
      !remoteEntries.exists(_.stat == local.stat)
    }.sortWith { (e1, e2) =>
      e1.stat.path.compareTo(e2.stat.path) < 0
    }
    println("diff entries size: " + diff.size)

    // TODO file and directory name based filter (aka $HOME/.dropsyncignore.conf)
    val (dirs, files) = diff.partition(_.isDir)
    dirs.foreach { dir =>
      println(s"D : ${dir.absPath}, ${remotePath + dir.stat.path}")
    }
    files.foreach { file =>
      println(s"F : ${file.absPath}, ${remotePath + file.stat.path}")
    }

    // Make directories (serial execution)
    dirs.foreach { dir =>
      println(s"making : r:${remotePath + dir.stat.path}, l:${dir.absPath}")
      client.mkdir(remotePath + dir.stat.path)
    }

    // Upload files (parallel execution)
    val pool = Executors.newFixedThreadPool(10) // TODO Should be configurable
    implicit val context = ExecutionContext.fromExecutorService(pool)

    val futures: Seq[Future[Entry]] = for (i <- 0 to files.size - 1) yield {
      val file = files(i)
      val f: Future[Entry] = Future {
        var start = System.currentTimeMillis
        println(s"uploading $i: r:${remotePath + file.stat.path}, l:${file.absPath}, size:${file.stat.size}")
        client.upload(file, remotePath)
        println(s"uploaded  $i: r:${remotePath + file.stat.path}, l:${file.absPath}, size:${file.stat.size}, uploadTime:${(System.currentTimeMillis - start) / 1000}")
        file
      }
      f.onSuccess {
        case entry: Entry => println(s"$i : onSuccess : $file")
      }
      f.onFailure {
        case t: Throwable => println(s"$i : onFailure : $file ,${t.getMessage}")
      }
      f
    }
    val result = Await.ready(Future.sequence(futures), Duration.Inf)
    println(s"Completed total: ${(System.currentTimeMillis - startTime) / 1000} sec")
    result.foreach { r =>
      println(s"success : ${r.size}")
      println(s"failure : ${files.size - r.size}")
    }
    val bytes = files.map(_.stat.size).reduceLeft(_ + _)
    println(s"Total Mega Bytes : ${bytes / 1024 / 1024}")
    sys.exit(0)
  }

  case class UploadResult(success: Boolean)

}
