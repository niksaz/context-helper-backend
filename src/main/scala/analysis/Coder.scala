package analysis

import java.io._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class Coder {
  private val DOCUMENT_ID_BYTES = 4
  private val COUNT_BYTES = 2

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

  def writeCompressedTypeIdQuestionCountList(
      fileName: File,
      typeIdQuestionCountList: List[ListBuffer[(Int, Int)]]): Unit = {
    val outputStream = new BufferedOutputStream(new FileOutputStream(fileName))
    typeIdQuestionCountList.foreach { itemList =>
      for ((documentId, count) <- itemList) {
        writeInt(documentId, DOCUMENT_ID_BYTES, outputStream)
        writeInt(count, COUNT_BYTES, outputStream)
      }
      writeInt(0, DOCUMENT_ID_BYTES, outputStream)
    }
    outputStream.close()
  }

  def readCompressedTypeIdQuestionCountList(
      fileName: File
  ): List[mutable.ListBuffer[(Int, Int)]] = {
    val inputStream = new BufferedInputStream(new FileInputStream(fileName))
    val typeIdQuestionCountList = new ListBuffer[mutable.ListBuffer[(Int, Int)]]
    var nextByte = inputStream.read()
    while (nextByte != -1) {
      val itemList = new ListBuffer[(Int, Int)]
      var readIntTuple = readInt(nextByte, DOCUMENT_ID_BYTES, inputStream)
      var documentId = readIntTuple._1
      nextByte = readIntTuple._2
      while (documentId != 0) {
        readIntTuple = readInt(nextByte, COUNT_BYTES, inputStream)
        val countId = readIntTuple._1
        nextByte = readIntTuple._2
        itemList.append((documentId, countId))
        readIntTuple = readInt(nextByte, DOCUMENT_ID_BYTES, inputStream)
        documentId = readIntTuple._1
        nextByte = readIntTuple._2
      }
      typeIdQuestionCountList.append(itemList)
    }
    inputStream.close()
    typeIdQuestionCountList.toList
  }
}
