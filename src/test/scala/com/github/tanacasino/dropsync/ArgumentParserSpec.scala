package com.github.tanacasino.dropsync

import com.github.tanacasino.dropsync.command.SyncCommand
import org.scalatest._

class ArgumentParserSpec extends FunSpec with Matchers {

  describe("parseArgs") {
    val syncCommand = "sync"

    describe("when no arguments") {
      it("should throw IllegalArgumentException") {
        intercept[IllegalArgumentException] {
          ArgumentParser.parseArgs(Array.empty)
        }
      }
    }

    describe("when sync command argument") {
      val local = "/Volume/home/photos"
      val remote = "/Backups/photos"

      it("should returns SyncCommand") {
        val args = Array(syncCommand, local, remote)
        ArgumentParser.parseArgs(args) match {
          case SyncCommand(l, r) => {
            assert(l == local)
            assert(r == remote)
          }
          case _ => assert(false)
        }
      }

      it("should throw IllegalArgumentException without remote path") {
        val args = Array(syncCommand, local)
        intercept[IllegalArgumentException] {
          ArgumentParser.parseArgs(args)
        }
      }
    }

    describe("when sync command without arguments") {
      it("should throw IllegalArgumentException with 0 argument") {
        intercept[IllegalArgumentException] {
          ArgumentParser.parseArgs(Array(syncCommand))
        }
      }
      it("should throw IllegalArgumentException with 1 argument") {
        intercept[IllegalArgumentException] {
          ArgumentParser.parseArgs(Array(syncCommand, "/Volume/home/photos"))
        }
      }
    }
  }

}
