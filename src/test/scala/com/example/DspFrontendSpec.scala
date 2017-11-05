package com.example

import org.scalatest.{BeforeAndAfterEach, FunSpec, Matchers}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._

class DspFrontendSpec extends FunSpec with Matchers with ScalatestRouteTest with BeforeAndAfterEach with MockitoSugar with BidResponseJsonFormats with DspFrontend {
  var repository: CampaignRepository = _
  override var service = new CampaignService(repository)

  override def beforeEach(): Unit = {
    repository = mock[CampaignRepository]
    service = new CampaignService(repository)

    when(repository.findByBundleNameAndConnectionTypeAndCountry("com.facebook", "127.0.0.1", "WiFi"))
      .thenReturn(Some(Campaign(3, "CocaCola Life", BigDecimal.valueOf(50000), "DE", "com.facebook", "WiFi")))
  }

  describe("DspFrontend") {
    it("should return a proper bid response for bid_request") {
      Get("/bid_request?auction_id=12&ip=127.0.0.1&bundle_name=com.facebook&connection_type=WiFi") ~> routes ~> check {
        status === StatusCodes.OK

        val response = responseAs[Bid]

        response.result shouldEqual "bid"
        response.auctionId shouldEqual "12"
        response.currency shouldEqual "USD"
        response.bid should (be  > 0.035 and be < 0.055)
      }
    }

    it("should return no_bid response for bid_request with invalid bundle name") {
      when(repository.findByBundleNameAndConnectionTypeAndCountry("com.invalid", "127.0.0.1", "WiFi"))
        .thenReturn(None)

      Get("/bid_request?auction_id=13&ip=127.0.0.1&bundle_name=com.invalid&connection_type=WiFi") ~> routes ~> check {
        status === StatusCodes.OK

        val response = responseAs[NoBid]

        response.result shouldEqual "no_bid"
        response.auctionId shouldEqual "13"
      }
    }

    it("should process and update budget for valid bid request") {
      Get("/bid_request?auction_id=14&ip=127.0.0.1&bundle_name=com.facebook&connection_type=WiFi") ~> routes ~> check {
        verify(repository).update(Campaign(3, "CocaCola Life", BigDecimal.valueOf(49999.955), "DE", "com.facebook", "WiFi"))
      }
    }

    it("should revert bid request if winner exceeds timeout") {
      when(repository.findByBundleNameAndConnectionTypeAndCountry("com.facebook", "127.0.0.1", "WiFi"))
        .thenReturn(Some(Campaign(3, "CocaCola Life", BigDecimal.valueOf(50000), "DE", "com.facebook", "WiFi")))
        .thenReturn(Some(Campaign(3, "CocaCola Life", BigDecimal.valueOf(49999.955), "DE", "com.facebook", "WiFi")))


      Get("/bid_request?auction_id=15&ip=127.0.0.1&bundle_name=com.facebook&connection_type=WiFi") ~> routes ~> check {
          Thread.sleep(600)
          verify(repository).update(Campaign(3, "CocaCola Life", BigDecimal.valueOf(49999.955), "DE", "com.facebook", "WiFi"))
          verify(repository).update(Campaign(3, "CocaCola Life", BigDecimal.valueOf(50000), "DE", "com.facebook", "WiFi"))
      }
    }

    it("should complete bid on bid winner") {
      Get("/bid_request?auction_id=16&ip=127.0.0.1&bundle_name=com.facebook&connection_type=WiFi") ~> routes ~> check {
        Get("/winner/16") ~> routes ~> check {
          status === StatusCodes.OK
          responseAs[String] shouldEqual "OK"

          Thread.sleep(600)
          verify(repository).findByBundleNameAndConnectionTypeAndCountry("com.facebook", "127.0.0.1", "WiFi")
          verify(repository).update(Campaign(3, "CocaCola Life", BigDecimal.valueOf(49999.955), "DE", "com.facebook", "WiFi"))
          verifyNoMoreInteractions(repository)
        }
      }
    }


    it ("should return a proper winner response for winner request") {
      Get("/winner/6c831376-c1df-43ef-a377-85d83aa3314c") ~> routes ~> check {
        status === StatusCodes.InternalServerError
        responseAs[String] shouldEqual "No entry found for given auction token. Bad Request or Possible Timeout"
      }
    }

  }
}
