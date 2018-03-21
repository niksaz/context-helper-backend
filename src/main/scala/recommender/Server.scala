package recommender

import java.io.PrintWriter
import java.net.ServerSocket
import java.util.Scanner

import scala.collection.mutable

object Server {
  def withResource[A <: AutoCloseable, B](closeable: A)(fun: (A) => B): B = {
    var t: Throwable = null
    try {
      fun(closeable)
    } catch {
      case funT: Throwable =>
        t = funT
        throw t
    } finally {
      if (t != null) {
        try {
          closeable.close()
        } catch {
          case closeT: Throwable =>
            t.addSuppressed(closeT)
            throw t
        }
      } else {
        closeable.close()
      }
    }
  }

  def main(args: Array[String]): Unit = {
    val typeProcessor = TypeProcessor()
    withResource(new ServerSocket(40000)) { serverSocket =>
      println("ServerSocket opened")
      while (true) {
        try {
          withResource(serverSocket.accept()) { socket =>
            val scanner = new Scanner(socket.getInputStream)
            val count = scanner.nextLine().toInt
            val types = mutable.ListBuffer[String]()
            Range(0, count).foreach { _ => types.append(scanner.nextLine()) }

            println(count)
            println(types)

            val printWriter = new PrintWriter(socket.getOutputStream, true)
            printWriter.println(1)
            printWriter.println(218384L)
            printWriter.flush()

            printWriter.close()
            scanner.close()
          }
        } catch {
          case t: Throwable => println(t.getMessage)
        }
      }
    }
  }
}
