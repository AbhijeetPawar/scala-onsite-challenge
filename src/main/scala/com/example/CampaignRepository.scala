package com.example

import scala.collection.mutable

object CampaignRepository {
  private val Campaigns = mutable.HashMap(
    1 -> new Campaign(1, "CocaCola Life", BigDecimal.valueOf(50000), "DE", "com.rovio.angry_birds", "WiFi"),
    2 -> new Campaign(2, "CocaCola Life", BigDecimal.valueOf(50000), "DE", "com.spotify", "WiFi"),
    3 -> new Campaign(3, "CocaCola Life", BigDecimal.valueOf(50000), "DE", "com.facebook", "WiFi")
  )

  def getById(id: Int): Option[Campaign] = Campaigns.synchronized {
      Campaigns.get(id)
  }

  def update(campaign: Campaign): Unit = Campaigns.synchronized {
    Campaigns.update(campaign.id, campaign)
  }
}
