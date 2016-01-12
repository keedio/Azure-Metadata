package com.keedio.azure.metadata

import org.junit.Test

/**
 * Created by rolmo on 12/1/16.
 */
class AzureMetadataUtilTest {

  @Test
  def testGoodCredentials: Unit = {
    val azTest = new AzureMetadataUtil("/Users/rolmo/repository/AzureMetadata/src/test/resources/azure.conf")
    val response: Response = azTest.getEventHubMetadata()

    assert(response.code == 200)
  }

  @Test
  def testBadCredentials: Unit = {
    val azTest = new AzureMetadataUtil("/Users/rolmo/repository/AzureMetadata/src/test/resources/azure.bad.conf")
    val response: Response = azTest.getEventHubMetadata()

    assert(response.code == 401)
  }

}
