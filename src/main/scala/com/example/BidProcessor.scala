package com.example

import akka.actor.{Actor, PoisonPill, ReceiveTimeout}

import scala.concurrent.duration.{Duration, MILLISECONDS}

object BidProcessor {
  case class ProcessBid(campaign: Campaign, bidAmount: Double)

  case class RevertBid(campaign: Campaign, bidAmount: Double)

  case class BidWinner()
}

class BidProcessor(auctionId: String, campaignRepository: CampaignRepository) extends Actor {
  import com.example.BidProcessor._
  import scala.concurrent.ExecutionContext.Implicits.global

  val lock = new Object

  override def receive: PartialFunction[Any, Unit] = {
    case ProcessBid(campaign, bidAmount) =>
      sender ! process(campaign, bidAmount)
      println(s"Bid with auctionId: `$auctionId` processed successfully")
      context.system.scheduler.scheduleOnce(Duration(500, MILLISECONDS), self, RevertBid(campaign, bidAmount))

    case RevertBid(campaign, bidAmount) =>
      revert(campaign, bidAmount)
      println(s"Bid with auctionId: `$auctionId` reverted successful")
      self ! PoisonPill

    case BidWinner =>
      println(s"Bid with auctionId: `$auctionId` successful")
      self ! PoisonPill

    case ReceiveTimeout =>
      println(s"Bid with auctionId: `$auctionId` timed-out")
      self ! PoisonPill
  }

  private def process(campaign: Campaign, bidAmount: Double): BidResponse = {
    campaignRepository.update(campaign.copy(Budget = campaign.Budget - BigDecimal.valueOf(bidAmount)))
    Bid(auctionId, bidAmount, "USD", "http://videos-bucket.com/video123.mov", "something")
  }

  private def revert(campaign: Campaign, bidAmount: Double): Unit = {
    campaignRepository.update(campaign.copy(Budget = campaign.Budget + BigDecimal.valueOf(bidAmount)))
  }
}
