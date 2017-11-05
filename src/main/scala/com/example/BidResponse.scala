package com.example

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

sealed trait BidResponse {
  val result: String
}

final case class Bid(auctionId: String,
                     bid: Double,
                     currency: String,
                     creative: String,
                     winningNotificationUrl: String,
                     result: String = "bid") extends BidResponse

final case class NoBid(auctionId: String, result: String = "no_bid") extends BidResponse

trait BidResponseJsonFormats extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val bidFormat: RootJsonFormat[Bid] = jsonFormat(Bid, "auction_id", "bid", "currency", "creative", "winning_notification", "result")
  implicit val noBidFormat: RootJsonFormat[NoBid] = jsonFormat(NoBid, "auction_id", "result")

  implicit object BidResponseFormat extends RootJsonFormat[BidResponse] {
    override def write(obj: BidResponse): JsValue = obj match {
      case b: Bid => b.toJson
      case nb: NoBid => nb.toJson
    }

    override def read(json: JsValue): BidResponse =
      json.asJsObject.getFields("result") match {
        case Seq(JsString("bid")) => json.convertTo[Bid]
        case Seq(JsString("no_bid")) => json.convertTo[NoBid]
      }
  }
}
