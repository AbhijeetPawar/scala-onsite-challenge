package com.example

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.{ActorMaterializer, Materializer}

object Boot extends DspFrontend {

  override implicit val system: ActorSystem = ActorSystem("dsp")
  override implicit val materializer: Materializer = ActorMaterializer()

  override var service: CampaignService = new CampaignService(new CampaignRepository())

  def main(args: Array[String]): Unit = {
    Http().bindAndHandle(routes, "localhost", 8080)
  }
}
