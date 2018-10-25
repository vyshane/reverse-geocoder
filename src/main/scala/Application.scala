// Copyright 2018 Vy-Shane Xie

package mu.node.reversegeocoder

import com.typesafe.scalalogging.Logger
import io.grpc.netty.NettyServerBuilder
import mu.node.healthttpd.Healthttpd
import wvlet.airframe._

import scala.concurrent.duration._
import scala.concurrent.Await

trait Application {
  private val logger = Logger[this.type]
  private val config = bind[Config]
  private val healthttpd = bind[Healthttpd]

  def run: Unit = {
    healthttpd.startAndIndicateNotReady()

    logger.info(s"Loading places from ${config.placesFilePath} ...")
    implicit val ctx = monix.execution.Scheduler.Implicits.global
    val kdTree = Await.result(PlacesFileReader.load(config.placesFilePath).runAsync, 10 minutes)
    logger.info("Finished loading places")

    val grpcServer = NettyServerBuilder
      .forPort(config.grpcPort)
//      .addService(reverseGeocoderService)
      .build()
      .start()

    healthttpd.indicateReady()
    grpcServer.awaitTermination()

    sys.ShutdownHookThread {
      grpcServer.shutdown()
      healthttpd.stop()
    }
  }
}
