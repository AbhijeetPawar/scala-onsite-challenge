package com.example

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import akka.stream.Materializer

import scala.concurrent.ExecutionContextExecutor

/*
 * Add your logic here. Feel free to rearrange the code as you see fit,
 * this is just a starting point.
 */
trait DspFrontend extends Directives with BidResponseJsonFormats {

  implicit val system: ActorSystem
//  implicit val executor: ExecutionContextExecutor
  implicit val materializer: Materializer

  val service: CampaignService

  val routes: Route =
    path("bid_request") {
      get {
        parameters('auction_id, 'ip, 'bundle_name, 'connection_type) { (auction_id, ip, bundle_name, connection_type) =>
          complete {
            service.bidRequest(auction_id, ip, bundle_name, connection_type)
          }
        }
      }
    } ~ path("winner" / Segment) { auction_id =>
      get {
        complete {
          service.bidWinner(auction_id)
          "OK"
        }
      }
    }
}
