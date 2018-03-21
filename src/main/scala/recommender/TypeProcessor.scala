package recommender

import java.io.File

import analysis.{Coder, DistributedAnalyzer}

class TypeProcessor(
  private val typeIdTable: Map[String, Int],
  private val typeIdQuestionCountArray: Array[List[(Int, Int)]],
  private val questionIdCountMap: Map[Int, Int]
) {
  
}

object TypeProcessor {
  private val TYPE_IDS = 1124678

  def apply(): TypeProcessor = {
    val startTime = System.currentTimeMillis()
    val coder = new Coder

    val typeIdTable = DistributedAnalyzer.restoreTypeIdTable()
    println(s"typeIdTable loaded ${System.currentTimeMillis() - startTime} ms")

    val typeIdQuestionCountArray =
      coder.readCompressedTypeIdQuestionCountArray(
        new File("index/compressedTypeIdQuestionCountMap"), TYPE_IDS)
    println(s"typeIdQuestionCountArray loaded ${System.currentTimeMillis() - startTime} ms")

    val questionIdCountMap =
      coder.readCompressedQuestionIdCountMap(new File("index/compressedQuestionIdCountMap"))
    println(s"questionIdCountMap loaded @${System.currentTimeMillis() - startTime} ms")

    new TypeProcessor(typeIdTable, typeIdQuestionCountArray, questionIdCountMap)
  }
}
