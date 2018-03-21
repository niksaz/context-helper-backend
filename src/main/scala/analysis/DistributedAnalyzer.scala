package analysis

import java.io._
import java.util.{Calendar, Scanner}

import ch.usi.inf.reveal.parsing.artifact.ArtifactSerializer
import ch.usi.inf.reveal.parsing.model.visitors.TypeNodeVisitor

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object DistributedAnalyzer {
  def restoreTypeIdTable(): Map[String, Int] = {
    val typeIdTable = mutable.Map[String, Int]()
    val mapFile = new File("index/TypeToTypeIdMap.txt")
    val scanner = new Scanner(mapFile)
    while (scanner.hasNext) {
      val line = scanner.nextLine()
      val semicolonIndex = line.indexOf(':')
      val typeName = line.substring(0, semicolonIndex)
      val id = line.substring(semicolonIndex + 1)
      typeIdTable.put(typeName, id.toInt)
    }
    scanner.close()
    typeIdTable.toMap
  }

  def restoreCountMap(): Map[String, Int] = {
    val mapFile = new File("data/CountMap127")
    val scanner = new Scanner(mapFile)
    val countMap = mutable.Map[String, Int]().withDefaultValue(0)
    while (scanner.hasNext) {
      val line = scanner.nextLine()
      val commaIndex = line.lastIndexOf(',')
      val className = line.substring(1, commaIndex)
      val count = line.substring(commaIndex + 1, line.length - 1)
      countMap.put(className, count.toInt)
    }
    scanner.close()
    countMap.toMap
  }

  def writeTypeToTypeIdMap(countMap: Map[String, Int]): Unit = {
    val printWriter = new PrintWriter(new File("index/TypeToTypeIdMap.txt"))
    countMap.zipWithIndex.foreach { case (item, index) =>
      printWriter.println(item._1 + ":" + index)
    }
    printWriter.close()
  }

  def runDistributedAnalysis(): Unit = {
    val typeIdTable = restoreTypeIdTable()

    val stormedDatasetDir = new File("/Users/niksaz/Downloads/stormed-dataset")
    val jsonFilePaths = stormedDatasetDir.listFiles()
    println(jsonFilePaths.length)

    // snaps 1 ... 8
    new Thread(new AnalyzeRangeTask(jsonFilePaths, typeIdTable, Range(0, 200000))).start()
    // snaps 9 ... 16
    new Thread(new AnalyzeRangeTask(jsonFilePaths, typeIdTable, Range(200000, 400000))).start()
    // snaps 17 ... 24
    new Thread(new AnalyzeRangeTask(jsonFilePaths, typeIdTable, Range(400000, 600000))).start()
    // snaps 25 ... 32
    new Thread(new AnalyzeRangeTask(jsonFilePaths, typeIdTable, Range(600000, 800000))).start()
    // snaps 33 ... 40
    new Thread(new AnalyzeRangeTask(jsonFilePaths, typeIdTable, Range(800000, 1000000))).start()
    // snaps 41 ... 51
    new Thread(new AnalyzeRangeTask(jsonFilePaths, typeIdTable, Range(1000000, 1269991))).start()
  }

  def computerQuestionIdTermsTable(): Map[Int, Int] = {
    val dataDir = new File("data")
    val questionIdTermsList =
      dataDir.listFiles()
        .filter(file => file.getName.startsWith("questionIdTermsList"))
        .sorted
        .toList
    val questionIdTermsTable = mutable.Map[Int, Int]()
    questionIdTermsList.foreach { questionIdTermsListFile =>
      val scanner = new Scanner(questionIdTermsListFile)
      while (scanner.hasNext) {
        val line = scanner.nextLine()
        val semicolonIndex = line.indexOf(':')
        val questionId = line.substring(0, semicolonIndex).toInt
        val terms = line.substring(semicolonIndex + 1).toInt
        questionIdTermsTable.put(questionId, terms)
      }
      scanner.close()
    }
    questionIdTermsTable.toMap
  }

  def restoreQuestionIdTermsTable(): Map[Int, Int] = {
    val questionIdTermsTable = mutable.Map[Int, Int]()
    val mapFile = new File("index/QuestionIdTermsMap.txt")
    val scanner = new Scanner(mapFile)
    while (scanner.hasNext) {
      val line = scanner.nextLine()
      val semicolonIndex = line.indexOf(':')
      val questionId = line.substring(0, semicolonIndex).toInt
      val terms = line.substring(semicolonIndex + 1).toInt
      questionIdTermsTable.put(questionId, terms)
    }
    scanner.close()
    questionIdTermsTable.toMap
  }

  def writeQuestionIdTermsTable(questionIdTermsTable: Map[Int, Int]): Unit = {
    val printWriter = new PrintWriter(new File("index/QuestionIdTermsMap.txt"))
    questionIdTermsTable.foreach { case (questionId, terms) =>
      printWriter.println(questionId + ":" + terms)
    }
    printWriter.close()
  }

  def writeTypeIdQuestionCountMap(
      fileName: File,
      typeIdQuestionCountList: List[ListBuffer[(Int, Int)]]): Unit = {
    val printWriter = new PrintWriter(fileName)
    typeIdQuestionCountList.zipWithIndex.foreach { case (list, index) =>
      printWriter.print(index + ":")
      printWriter.println(list.map(pr => pr._1 + "," + pr._2).mkString(";"))
    }
    printWriter.close()
  }

//  def zipTypeIdQuestionCountMap(
//    typeIdQuestionCountList: List[mutable.ListBuffer[(Int, Int)]]
//  ): Unit = {
//    val typeIdQuestionCountMapFiles = List(
//      "data/typeIdQuestionCountMap08",
//      "data/typeIdQuestionCountMap16",
//      "data/typeIdQuestionCountMap24",
//      "data/typeIdQuestionCountMap32",
//      "data/typeIdQuestionCountMap40",
//      "data/typeIdQuestionCountMap51"
//    ).map(name => new File(name))
//
//    val coder = new Coder
//    typeIdQuestionCountMapFiles.foreach { partialMapFile =>
//      println(partialMapFile)
//      val scanner = new Scanner(partialMapFile)
//      var linesRead = 0
//      while (scanner.hasNext) {
//        val line = scanner.nextLine()
//        val questionCount = mutable.ListBuffer[(Int, Int)]()
//        val semicolonIndex = line.indexOf(':')
//        val typeId = line.substring(0, semicolonIndex).toInt
//        val questionCounts = line.substring(semicolonIndex + 1)
//        if (questionCounts.nonEmpty) {
//          questionCounts.split(";").foreach { questionCountString =>
//            val commaIndex = questionCountString.indexOf(',')
//            val questionId = questionCountString.substring(0, commaIndex).toInt
//            val count = questionCountString.substring(commaIndex + 1).toInt
//            questionCount.append((questionId, count))
//          }
//        }
//        typeIdQuestionCountList(typeId).appendAll(questionCount)
//
//        linesRead += 1
//        if (linesRead % 100000 == 0) {
//          println(s"$linesRead @ " + Calendar.getInstance.getTime)
//          coder.writeCompressedTypeIdQuestionCountList(
//            partialMapFile.toPath.getParent.resolve(
//              "compressed" + partialMapFile.getName.capitalize).toFile,
//            typeIdQuestionCountList)
//        }
//      }
//      scanner.close()
//      coder.writeCompressedTypeIdQuestionCountList(
//        partialMapFile.toPath.getParent.resolve("compressed" + partialMapFile.getName).toFile,
//        typeIdQuestionCountList)
//    }
//  }

  def main(args: Array[String]): Unit = {
//    val questionIdTermsTable = restoreQuestionIdTermsTable()
//    println(questionIdTermsTable.count { case (_, terms) => terms != 0 })

//    writeQuestionIdTermsTable(questionIdTermsTable)
//    println(s"${System.currentTimeMillis() - startTime}: completed questionIdTermsTable")
//
//    val countMap = restoreCountMap()
//    println(s"${System.currentTimeMillis() - startTime}: completed countMap")

//    val stormedDataset = new File("/Users/niksaz/Downloads/stormed-dataset")
//    val jsonFilePaths = stormedDataset.listFiles()
//    val jsonFilePath = jsonFilePaths(0)
//
//    val artifact = ArtifactSerializer.deserializeFromFile(jsonFilePath)
//    val questionId = artifact.question.id
//    val listVisitor = TypeNodeVisitor.list()
//
//    return

//    val typeIdTable = restoreTypeIdTable()
//    val typeIdQuestionCountArray = mutable.ListBuffer[mutable.ListBuffer[(Int, Int)]]()
//    (0 to typeIdTable.size).foreach { _ =>
//      typeIdQuestionCountArray.append(mutable.ListBuffer())
//    }
//    zipTypeIdQuestionCountMap(typeIdQuestionCountArray.toList)

    val typesCount = 1124678

    val startTime = System.currentTimeMillis()

    val coder = new Coder
    val typeIdQuestionCountArray =
      coder.readCompressedTypeIdQuestionCountArray(
        new File("index/compressedTypeIdQuestionCountMap"), 1124678)
    println(s"${System.currentTimeMillis() - startTime} ms")
    println(typeIdQuestionCountArray.length)
    println(typeIdQuestionCountArray.count(list => list.nonEmpty))
  }
}
