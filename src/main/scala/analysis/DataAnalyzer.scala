package analysis

import java.io.File
import java.util.Scanner

import scala.collection.mutable

object DataAnalyzer {
  def printStatistics(): Unit = {
    val contextsPath = new File("/Users/niksaz/Downloads/data/contexts")
    val idMap = mutable.Map[String, List[File]]().withDefaultValue(List())
    contextsPath.listFiles().foreach { file =>
      val scanner = new Scanner(file)
      val installId = scanner.nextLine()
      // blacklist installId of the test IntelliJ IDEA
      if (installId != "a590add0-f56f-4053-8b65-b27fe89c239d") {
        val files = idMap(installId)
        idMap.put(installId, file :: files)
      }
    }
    println(s"Users: ${idMap.size}")
    println(s"Contexts: ${idMap.map(_._2.size).sum}")
    for ((id, files) <- idMap) {
      println(s"$id ->")
      println(files.sortBy(_.getName.toInt))
    }
  }

  def main(args: Array[String]): Unit = {
    printStatistics()
  }
}
