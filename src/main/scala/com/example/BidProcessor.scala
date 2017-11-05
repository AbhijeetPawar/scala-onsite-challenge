package com.example

import akka.actor.{Actor, PoisonPill, ReceiveTimeout}

import scala.concurrent.duration.{Duration, MILLISECONDS}

object BidProcessor {
  case class ProcessBid(bidAmount: Double)

  case class RevertBid(bidAmount: Double)

  case class BidWinner()
}

class BidProcessor(campaignRepository: CampaignRepository,
                   auctionId: String, ip: String, bundleName: String, connectionType: String) extends Actor {
  import com.example.BidProcessor._

  import scala.concurrent.ExecutionContext.Implicits.global

  private val lock = new Object

  override def receive: PartialFunction[Any, Unit] = {
    case ProcessBid(bidAmount) =>
      sender ! process(bidAmount)
      println(s"Bid with auctionId: `$auctionId` processed successfully")
      context.system.scheduler.scheduleOnce(Duration(500, MILLISECONDS), self, RevertBid(bidAmount))

    case RevertBid(bidAmount) =>
      revert(bidAmount)
      println(s"Bid with auctionId: `$auctionId` reverted successful")
      self ! PoisonPill

    case BidWinner =>
      println(s"Bid with auctionId: `$auctionId` successful")
      self ! PoisonPill

    case ReceiveTimeout =>
      println(s"Bid with auctionId: `$auctionId` timed-out")
      self ! PoisonPill
  }

  private def process(bidAmount: Double): BidResponse = lock.synchronized {
    campaignRepository
      .findByBundleNameAndConnectionTypeAndCountry(bundleName, ip, connectionType)
      .map(campaign => {
        if (campaign.Budget >= BigDecimal.valueOf(bidAmount)) {
          campaignRepository.update(campaign.copy(Budget = campaign.Budget - BigDecimal.valueOf(bidAmount)))
          Bid(auctionId, bidAmount, "USD", "http://videos-bucket.com/video123.mov", "something")
        } else {
          NoBid(auctionId)
        }
      })
      .getOrElse(NoBid(auctionId))
  }

  private def revert(bidAmount: Double): Unit = lock.synchronized {
    campaignRepository
      .findByBundleNameAndConnectionTypeAndCountry(bundleName, ip, connectionType)
      .foreach(campaign => {
        campaignRepository.update(campaign.copy(Budget = campaign.Budget + BigDecimal.valueOf(bidAmount)))
      })
  }
}
