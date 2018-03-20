package ch.usi.inf.reveal.parsing.stormed.service

import java.security.cert.X509Certificate
import org.json4s.native.Serialization.{read, write}
import ch.usi.inf.reveal.parsing.artifact.ArtifactSerializer
import javax.net.ssl._
import scalaj.http.Http
import java.security.SecureRandom
import org.json4s.JsonAST.JObject


object StormedService {
  implicit val formats = ArtifactSerializer.formats  

  val trustManager = new X509TrustManager() {
    override def getAcceptedIssuers() = null
    override def checkClientTrusted(certs: Array[X509Certificate], authType: String): Unit = () 
    override def checkServerTrusted(certs: Array[X509Certificate], authType: String): Unit = ()
  }
  
  val trustAllCerts = Array[TrustManager](trustManager)
  val sc = SSLContext.getInstance("SSL")
  sc.init(null, trustAllCerts, new SecureRandom())
  HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory())
 
  HttpsURLConnection.setDefaultHostnameVerifier( new HostnameVerifier(){
    def verify(hostname: String, sslSession: javax.net.ssl.SSLSession ) = true
  })
  
  private[this] def doRestRequest[T <: Request](service: String, params: T) = {
    val url = s"https://stormed.inf.usi.ch/service/$service"
    val jsonRequest = write(params)
    Http(url).postData(jsonRequest)
      .header("Content-Type", "application/json")
      .header("Charset", "UTF-8")
      .asString.body
  }
  
  private[this] def hasError(response: String) = {
    import org.json4s.native.JsonMethods._
    
    parseOpt(response) match {
      case Some(json) =>
        val status = (json \ "status").extract[String]
        if(status == "ERROR")
          Some(read[ErrorResponse](response))
        else 
          None
      case None => 
        Some(ErrorResponse(s"Invalid Response: $response", "ERROR"))
    }
  }
  
  
  private def apiKey = "7BE636C9B7FED557F8CE30C2776C0F724BEEC889E8E6EE0B7F077A37D8205C20"
  

  
  def parse(text: String): Response = {
    val request = ParsingRequest(text, apiKey)
    val response = doRestRequest("parse", request)
    hasError(response) match {
      case Some(error) => error
      case None => read[ParsingResponse](response) 
    }
  }
  
  
  def tag(text: String, isTagged: Boolean): Response = {
    val request = TaggingRequest(text, isTagged, apiKey)
    val response = doRestRequest("tagger", request)
    hasError(response) match {
      case Some(error) => error
      case None => read[TaggingResponse](response) 
    }
  }
}