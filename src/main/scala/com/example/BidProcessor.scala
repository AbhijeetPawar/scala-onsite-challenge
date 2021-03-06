package com.example

import akka.actor.{Actor, Cancellable, PoisonPill, ReceiveTimeout}

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

  override def receive: PartialFunction[Any, Unit] = {
    case ProcessBid(bidAmount) =>
      sender ! process(bidAmount)
      println(s"Bid with auctionId: `$auctionId` processed")
      val cancellable = context.system.scheduler.scheduleOnce(Duration(500, MILLISECONDS), self, RevertBid(bidAmount))
      context.become(waitOnAck(cancellable))

    case ReceiveTimeout =>
      println(s"Bid with auctionId: `$auctionId` timed-out")
      self ! PoisonPill
  }

  private def waitOnAck(cancellable: Cancellable): Receive = {
    case BidWinner =>
      cancellable.cancel()
      println(s"Bid with auctionId: `$auctionId` completed")
      self ! PoisonPill

    case RevertBid(bidAmount) =>
      revert(bidAmount)
      println(s"Bid with auctionId: `$auctionId` reverted")
      self ! PoisonPill
  }

  private def process(bidAmount: Double): BidResponse = campaignRepository.synchronized {
    campaignRepository
      .findByBundleNameAndConnectionTypeAndCountry(bundleName, ip, connectionType)
      .map(campaign => {
        if (campaign.Budget >= BigDecimal.valueOf(bidAmount)) {
          campaignRepository.update(campaign.copy(Budget = campaign.Budget - BigDecimal.valueOf(bidAmount)))
          Bid(auctionId, bidAmount, "USD", "some-creative", s"http://localhost:8080/winner/$auctionId")
        } else {
          NoBid(auctionId)
        }
      })
      .getOrElse(NoBid(auctionId))
  }

  private def revert(bidAmount: Double): Unit = campaignRepository.synchronized {
    campaignRepository
      .findByBundleNameAndConnectionTypeAndCountry(bundleName, ip, connectionType)
      .foreach(campaign => {
        campaignRepository.update(campaign.copy(Budget = campaign.Budget + BigDecimal.valueOf(bidAmount)))
      })
  }
}
