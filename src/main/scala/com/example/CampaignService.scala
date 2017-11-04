package com.example

import java.util.concurrent.{ConcurrentHashMap, TimeUnit}

import akka.pattern.ask
import akka.actor.{ActorContext, ActorRef, ActorSystem, PoisonPill, Props}
import akka.util.Timeout
import com.example.CampaignServiceProtocol.ProcessBid

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class CampaignService(implicit system: ActorSystem) {

  import scala.concurrent.ExecutionContext.Implicits.global

  private val workers = new ConcurrentHashMap[String, ActorRef]()

  def bidRequest(auctionId: String, ip: String, bundleName: String, connectionType: String): Future[BidResponse] = {
    CampaignRepository
      .findByBundleNameAndConnectionTypeAndCountry(bundleName, ip, connectionType)
      .map(campaign => processBid(auctionId, campaign))
      .getOrElse(Future(NoBid(auctionId)))
  }

  private def processBid(auctionId: String, campaign: Campaign): Future[BidResponse] = {
    implicit val timeout: Timeout = Timeout(1, TimeUnit.SECONDS)

    val processorRef = system.actorOf(Props(classOf[BidProcessor], auctionId), "BidProcessor:" + auctionId)
    workers.put(auctionId, processorRef)

    ask(processorRef, ProcessBid(campaign, 0.045))
      .mapTo[BidResponse]
      .recover { case _ => NoBid(auctionId) }
  }

  def bidWinner(auctionId: String): Unit = {
    val processorRef = workers.remove(auctionId)

    if (processorRef != null) {
      processorRef ! PoisonPill
    }
  }
}
