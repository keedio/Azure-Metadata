package com.keedio.azure.metadata

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.apache.commons.codec.binary.Base64
import java.net.URLEncoder
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import javax.net.ssl.HttpsURLConnection
import java.net.URL
import java.util.Date
import javax.xml.bind.DatatypeConverter
import java.util.Properties
import java.io.InputStream
import java.io.IOException
//remove if not needed
import scala.collection.JavaConversions._


class Response(val theCode: Int, val theResponse: String) {
  var code:Int = theCode
  var response: String = theResponse
}


class AzureMetadataUtil(val file: String) {

  val prop = new Properties()
  try {
    prop.load(new java.io.FileInputStream(file))
  } catch {
    case e: IOException => {
      println("config file not found")
      println(e.getMessage)
      System.exit(-1)
    }
  }

  def getEventHubMetadata(): Response = {

    try {

      val key = prop.getProperty("sasKeyValue")
      val keyName = prop.getProperty("sasKeyName")
      val resourceUri = prop.getProperty("eventhubUri")
      val debug = prop.getProperty("Debug")

      val targetUri = URLEncoder.encode(resourceUri.toLowerCase(), "UTF-8")
        .toLowerCase()
      val expiration = Math.round((new Date(System.currentTimeMillis() + 0x493e0L)).getTime /
        1000L)
      val sb = new StringBuilder()
      sb.append(targetUri)
      sb.append("\n").append(expiration)
      val signature = sb.toString
      val signingKey = new SecretKeySpec(key.getBytes, "HmacSHA256")
      val mac = Mac.getInstance("HmacSHA256")
      mac.init(signingKey)
      val rawHmac = mac.doFinal(signature.getBytes)
      val hmac = URLEncoder.encode(DatatypeConverter.printBase64Binary(rawHmac), "UTF-8")
      val sasToken = String.format("SharedAccessSignature sr=%s&sig=%s&se=%d&skn=%s", targetUri, hmac, java.lang.Integer.valueOf(expiration), keyName)

      val obj = new URL(resourceUri)
      val con = obj.openConnection().asInstanceOf[HttpsURLConnection]
      con.setRequestMethod("GET")
      con.setUseCaches(false)
      con.setRequestProperty("Authorization", sasToken)
      con.setConnectTimeout(3000)
      var eventData = "event data from a little IoT device"

      con.setDoOutput(true)
      val responseCode = con.getResponseCode
      val in = new BufferedReader(new InputStreamReader(con.getInputStream))
      var inputLine: String = null
      val response = new StringBuffer()
      Stream.continually(in.readLine())
        .takeWhile(_ != null)
        .foreach(response.append(_))

      in.close()
      new Response(responseCode, response.toString)
    } catch {
      case e: IOException => new Response(401, "Unauthorized")
    }

  }
}

