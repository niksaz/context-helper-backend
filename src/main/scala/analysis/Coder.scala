package analysis

import java.io._

import scala.collection.mutable

class Coder {
  private val DOCUMENT_ID_BYTES = 4
  private val COUNT_BYTES = 2

  def writeCompressedTypeIdQuestionCountArray(
      fileName: File,
      typeIdQuestionCountArray: Array[List[(Int, Int)]]): Unit = {
    val outputStream = new BufferedOutputStream(new FileOutputStream(fileName))
    typeIdQuestionCountArray.foreach { itemList =>
      for ((documentId, count) <- itemList) {
        writeInt(documentId, DOCUMENT_ID_BYTES, outputStream)
        writeInt(count, COUNT_BYTES, outputStream)
      }
      writeInt(0, DOCUMENT_ID_BYTES, outputStream)
    }
    outputStream.close()
  }

  def readCompressedTypeIdQuestionCountArray(
    fileName: File,
    sizeHint: Int
  ): Array[List[(Int, Int)]] = {
    val typeIdQuestionCountArray = Array.fill[List[(Int, Int)]](sizeHint) { null }
    val inputStream = new BufferedInputStream(new FileInputStream(fileName))
    var nextByte = inputStream.read()
    val itemList = mutable.ListBuffer[(Int, Int)]()
    var arrayIndex = 0
    while (nextByte != -1) {
      var readIntTuple = readInt(nextByte, DOCUMENT_ID_BYTES, inputStream)
      var documentId = readIntTuple._1
      nextByte = readIntTuple._2
      while (documentId != 0) {
        readIntTuple = readInt(nextByte, COUNT_BYTES, inputStream)
        val count = readIntTuple._1
        nextByte = readIntTuple._2
        itemList.append((documentId, count))
        readIntTuple = readInt(nextByte, DOCUMENT_ID_BYTES, inputStream)
        documentId = readIntTuple._1
        nextByte = readIntTuple._2
      }
      typeIdQuestionCountArray(arrayIndex) = itemList.toList
      arrayIndex += 1
      itemList.clear()
    }
    inputStream.close()
    typeIdQuestionCountArray
  }

  def writeCompressedQuestionIdCountMap(fileName: File, questionIdCountMap: Map[Int, Int]): Unit = {
    val outputStream = new BufferedOutputStream(new FileOutputStream(fileName))
    questionIdCountMap.foreach { case (questionId, count) =>
        writeInt(questionId, DOCUMENT_ID_BYTES, outputStream)
        writeInt(count, COUNT_BYTES, outputStream)
    }
    outputStream.close()
  }

  def readCompressedQuestionIdCountMap(fileName: File): Map[Int, Int] = {
    val questionIdCountMap = mutable.Map[Int, Int]()
    val inputStream = new BufferedInputStream(new FileInputStream(fileName))
    var nextByte = inputStream.read()
    while (nextByte != -1) {
      var readIntTuple = readInt(nextByte, DOCUMENT_ID_BYTES, inputStream)
      val questionId = readIntTuple._1
      nextByte = readIntTuple._2
      readIntTuple = readInt(nextByte, COUNT_BYTES, inputStream)
      val count = readIntTuple._1
      nextByte = readIntTuple._2
      questionIdCountMap.put(questionId, count)
    }
    inputStream.close()
    questionIdCountMap.toMap
  }

  private def writeInt(value: Int, bytes: Int, outputStream: OutputStream): Unit = {
    var leftValue = value
    Range(0, bytes).foreach { _ =>
      outputStream.write(leftValue % 256)
      leftValue /= 256
    }
  }

  private def readInt(currentByte: Int, bytes: Int, inputStream: InputStream): (Int, Int) = {
    var resultValue = 0
    var nextByte = currentByte
    Range(0, bytes).foreach { index =>
      resultValue += nextByte << (8 * index)
      nextByte = inputStream.read()
    }
    (resultValue, nextByte)
  }
}
