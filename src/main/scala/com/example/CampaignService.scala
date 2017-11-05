package com.example

import java.util.concurrent.{ConcurrentHashMap, TimeUnit}

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.example.BidProcessor.{BidWinner, ProcessBid}

import scala.concurrent.Future

class CampaignService(campaignRepository: CampaignRepository)(implicit system: ActorSystem) {
  import scala.concurrent.ExecutionContext.Implicits.global

  private val workers = new ConcurrentHashMap[String, ActorRef]()

  def bidRequest(auctionId: String, ip: String, bundleName: String, connectionType: String): Future[BidResponse] = {
    implicit val timeout: Timeout = Timeout(1, TimeUnit.SECONDS)

    val processorRef = system.actorOf(Props(classOf[BidProcessor], campaignRepository, auctionId, ip, bundleName, connectionType), "BidProcessor:" + auctionId)
    workers.put(auctionId, processorRef)

    ask(processorRef, ProcessBid(0.045))
      .mapTo[BidResponse]
      .recover { case _ => NoBid(auctionId) }
  }

  def bidWinner(auctionId: String): Unit = {
    val processorRef = workers.remove(auctionId)

    if (processorRef != null) {
      processorRef ! BidWinner
    }
  }
}
