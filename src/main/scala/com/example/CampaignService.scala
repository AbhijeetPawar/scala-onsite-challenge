package com.example

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.example.BidProcessor.{BidWinner, ProcessBid}

import scala.concurrent.Future
import scala.util.Success

class CampaignService(campaignRepository: CampaignRepository)(implicit system: ActorSystem) {
  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val timeout: Timeout = Timeout(1, TimeUnit.SECONDS)

  private val processorName: String => String = auctionId => "BidProcessor:" + auctionId

  def bidRequest(auctionId: String, ip: String, bundleName: String, connectionType: String): Future[BidResponse] = {
    val processorRef = system.actorOf(Props(classOf[BidProcessor], campaignRepository, auctionId, ip, bundleName, connectionType), processorName(auctionId))
    ask(processorRef, ProcessBid(0.045))
      .mapTo[BidResponse]
      .recover { case _ => NoBid(auctionId) }
  }

  def bidWinner(auctionId: String): Future[ActorRef] = {
    system.actorSelection("/user/" + processorName(auctionId)).resolveOne andThen {
      case Success(actorRef) => actorRef ! BidWinner
    }
  }
}
