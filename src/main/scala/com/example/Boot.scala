package com.example

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.stream.{ActorMaterializer, Materializer}

import scala.concurrent.ExecutionContextExecutor

object Boot extends DspFrontend {

  override implicit val system: ActorSystem = ActorSystem("simple-dsp")
//  override implicit val executor: ExecutionContextExecutor = system.dispatcher
  override implicit val materializer: Materializer = ActorMaterializer()
  override val service: CampaignService = new CampaignService()

  def main(args: Array[String]): Unit = {
    Http().bindAndHandle(routes, "localhost", 8080)
  }
}
