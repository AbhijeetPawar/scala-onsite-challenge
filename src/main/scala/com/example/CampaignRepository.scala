package com.example

import scala.collection.mutable

class CampaignRepository {
  private val Campaigns = mutable.HashMap(
    1 -> Campaign(1, "CocaCola Life", BigDecimal.valueOf(50000), "DE", "com.rovio.angry_birds", "WiFi"),
    2 -> Campaign(2, "CocaCola Life", BigDecimal.valueOf(50000), "DE", "com.spotify", "WiFi"),
    3 -> Campaign(3, "CocaCola Life", BigDecimal.valueOf(50000), "DE", "com.facebook", "WiFi")
  )

  def getById(id: Int): Option[Campaign] = {
      Campaigns.get(id)
  }

  def findByBundleNameAndConnectionTypeAndCountry(bundleName: String, ip: String, connectionType: String): Option[Campaign] = {
    Campaigns.values.find(campaign => campaign.connectionType.equals(connectionType) && campaign.mobileApp.equals(bundleName))
  }

  def update(campaign: Campaign): Unit = {
    Campaigns.update(campaign.id, campaign)
  }
}
