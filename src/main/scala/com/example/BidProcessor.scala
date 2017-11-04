package com.example

import akka.actor.Actor
import com.example.CampaignServiceProtocol.ProcessBid

class BidProcessor(auctionId: String) extends Actor {

  override def receive: PartialFunction[Any, Unit] = {
    case ProcessBid(campaign, bidAmount) =>
      sender ! Bid(auctionId, bidAmount, "USD", "http://videos-bucket.com/video123.mov", "something")

  }
}
