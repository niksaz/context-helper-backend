package analysis

import java.io.{File, PrintWriter}
import java.util.Calendar

import ch.usi.inf.reveal.parsing.artifact.ArtifactSerializer
import ch.usi.inf.reveal.parsing.model.visitors.TypeNodeVisitor

import scala.collection.mutable

class AnalyzeRangeTask(
    private val jsonFilePaths: Array[File],
    private val typeIdTable: Map[String, Int],
    private val range: Range) extends Runnable {
  private val THRESHOLD = 25000

  override def run(): Unit = {
    val threadID = Thread.currentThread().getId
    println("STARTED " + threadID)

    def printTypeQuestionCount(fId: Int, typeIdQuestionCountList: mutable.ListBuffer[mutable.ListBuffer[(Int, Int)]]): Unit = {
      val filename = "data/typeIdQuestionCountMap" + fId
      val printWriter = new PrintWriter(new File(filename))
      typeIdQuestionCountList.zipWithIndex.foreach { case (list, index) =>
        printWriter.print(index + ":")
        printWriter.println(list.map(pr => pr._1 + "," + pr._2).mkString(";"))
      }
      printWriter.close()
      println(s"wrote to $filename")
    }

    def printQuestionIdTerms(fId: Int, questionIdTermsList: mutable.ListBuffer[(Int, Int)]): Unit = {
      val filename = "data/questionIdTermsList" + fId
      val printWriter = new PrintWriter(new File(filename))
      questionIdTermsList.foreach { case (questionId, termsCount) =>
        printWriter.println(questionId + ":" + termsCount)
      }
      printWriter.close()
      println(s"wrote to $filename")
    }

    val typeIdQuestionCountList = mutable.ListBuffer[mutable.ListBuffer[(Int, Int)]]()
    (0 to typeIdTable.size).foreach { _ =>
      typeIdQuestionCountList.append(mutable.ListBuffer())
    }
    val questionIdTermsList = mutable.ListBuffer[(Int, Int)]()

    var counter = range.start
    jsonFilePaths.slice(range.start, range.end).foreach { jsonFilePath =>
      val artifact = ArtifactSerializer.deserializeFromFile(jsonFilePath)
      val questionId = artifact.question.id
      val listVisitor = TypeNodeVisitor.list()
      val collected = listVisitor(List(), artifact)
      val typeCountMap = mutable.Map[String, Int]().withDefaultValue(0)
      for (node <- collected) {
        val count = typeCountMap(node.simpleName)
        typeCountMap.update(node.simpleName, count + 1)
      }

      var termsInDocument = 0
      for ((typeName, count) <- typeCountMap) {
        termsInDocument += count
        val typeId = typeIdTable(typeName)
        typeIdQuestionCountList(typeId).append((questionId, count))
      }
      questionIdTermsList.append((questionId, termsInDocument))

      counter += 1
      if (counter % THRESHOLD == 0) {
        val fileIndex = counter / THRESHOLD
        printTypeQuestionCount(fileIndex, typeIdQuestionCountList)
        printQuestionIdTerms(fileIndex, questionIdTermsList)
        println(s"[$threadID] reached $counter @ " + Calendar.getInstance.getTime)
      }
    }
    if (counter % THRESHOLD != 0) {
      val fileIndex = counter / THRESHOLD + 1
      printTypeQuestionCount(fileIndex, typeIdQuestionCountList)
      printQuestionIdTerms(fileIndex, questionIdTermsList)
      println(s"[$threadID] reached $counter @ " + Calendar.getInstance.getTime)
    }
  }
}