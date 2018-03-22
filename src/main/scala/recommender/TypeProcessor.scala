package recommender

import java.io.File

import analysis.{Coder, DistributedAnalyzer}

import scala.collection.mutable

class TypeProcessor(
  private val typeIdTable: Map[String, Int],
  private val typeIdQuestionCountArray: Array[List[(Int, Int)]],
  private val questionIdCountMap: Map[Int, Int],
  private val questionsScores: Map[Int, Int]
) {
  private val termfulDocuments = questionIdCountMap.count { _._2 != 0 }

  def recommendThreads(types: List[String]): List[(Int, Double)] = {
    val scoredQuestions =
      mutable.Map[Int, Double]().withDefault(id => postScoreToScore(questionsScores(id)))
    types
      .map(typeName => typeIdTable.get(typeName))
      .filter(option => option.isDefined)
      .map(option => option.get)
      .foreach { typeId =>
        val encountered = typeIdQuestionCountArray(typeId)
        val idf = Math.log(termfulDocuments / encountered.size.toDouble)
        encountered.foreach { case (questionId, count) =>
          val frequency = count.toDouble / questionIdCountMap(questionId)
          val score = scoredQuestions(questionId)
          scoredQuestions.put(questionId, score + frequency * idf)
        }
      }
    scoredQuestions.toSeq.sortBy(_._2).reverse.take(10).toList
  }

  private def postScoreToScore(postScore: Int): Double =
    if (postScore <= 0) 0.0 else Math.log(postScore)
}

object TypeProcessor {
  private val TYPE_IDS = 1124678

  def apply(): TypeProcessor = {
    val startTime = System.currentTimeMillis()
    val coder = new Coder

    val typeIdTable = DistributedAnalyzer.restoreTypeIdTable()
    println(s"typeIdTable loaded @${System.currentTimeMillis() - startTime} ms")

    val typeIdQuestionCountArray =
      coder.readCompressedTypeIdQuestionCountArray(
        new File("index/compressedTypeIdQuestionCountMap"), TYPE_IDS)
    println(s"typeIdQuestionCountArray loaded @${System.currentTimeMillis() - startTime} ms")

    val questionIdCountMap =
      coder.readCompressedQuestionIdCountMap(new File("index/compressedQuestionIdCountMap"))
    println(s"questionIdCountMap loaded @${System.currentTimeMillis() - startTime} ms")

    val questionsScores = DistributedAnalyzer.restoreQuestionsScores()
    println(s"questionsScores loaded @${System.currentTimeMillis() - startTime} ms")

    new TypeProcessor(typeIdTable, typeIdQuestionCountArray, questionIdCountMap, questionsScores)
  }
}
