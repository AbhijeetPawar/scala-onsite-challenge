package com.example

object CampaignServiceProtocol {
  case class ProcessBid(campaign: Campaign, bidAmount: Double)

  case class BidWinner(auctionId: String)
}
