package com.example

import org.scalatest.{FunSpec, Matchers}


class CampaignRepositorySpec extends FunSpec with Matchers {
  val repository = new CampaignRepository()

  describe("Campaign Repository") {
    it ("should return a campaign given id") {
      val campaign = repository.getById(1)
      campaign === Campaign(1, "CocaCola Life", BigDecimal.valueOf(50000), "DE", "com.rovio.angry_birds", "WiFi")
    }
  }
}
