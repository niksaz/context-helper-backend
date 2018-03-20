package analysis

import ch.usi.inf.reveal.parsing.stormed.service.{ErrorResponse, ParsingResponse, StormedService}

object StormedClientExample {
  def main(args: Array[String]): Unit = {
    val codeToParse = """
    post.setHeader("User-Agent", USER_AGENT);
    List<NameValuePair> urlParameters = new ArrayList<>();
    urlParameters.resizeResizeResize
    """.trim


    val result = StormedService.parse(codeToParse)
    result match {
      case ParsingResponse(result, quota, status) =>
        println(s"Status: $status")
        println(s"Quota Remaining: $quota")
        val nodeTypes = result.map{_.getClass.getSimpleName}
        println("Parsing Result: ")
        nodeTypes.foreach{println}
      case ErrorResponse(message, status) =>
        println(status + ": " + message)
    }
  }
}
