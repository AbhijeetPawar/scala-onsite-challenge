package com.example

class CampaignService {
  def bidRequest(auctionId: String, ip: String, bundleName: String, connectionType: String): BidResponse = {
    Bid(auctionId, 0.045, "USD", "http://videos-bucket.com/video123.mov", "something")
  }

  def bidWinner(auctionId: String): Unit = {

  }
}
