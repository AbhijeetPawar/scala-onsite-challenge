package com.example

import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route

/*
 * Add your logic here. Feel free to rearrange the code as you see fit,
 * this is just a starting point.
 */
object DspFrontend extends Directives with BidResponseJsonFormats {

  import scala.concurrent._
  import ExecutionContext.Implicits.global

  val service = new CampaignService();

  def apply(): Route =
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
