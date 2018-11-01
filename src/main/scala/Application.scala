// Copyright 2018 Vy-Shane Xie

package mu.node.reversegeocoder

import com.typesafe.scalalogging.LazyLogging
import io.grpc.netty.NettyServerBuilder
import monix.execution.Scheduler.Implicits.global
import mu.node.healthttpd.Healthttpd
import wvlet.airframe._

import scala.concurrent.Await
import scala.concurrent.duration._

trait Application extends LazyLogging {
  private val config = bind[Config]
  private val healthttpd = bind[Healthttpd]
  private val placesLoader = bind[PlacesLoader]

  def run: Unit = {
    healthttpd.startAndIndicateNotReady()

    val places = Await.result(placesLoader.load().runAsync, 1 minute)
    val reverseGeocoderService = new ReverseGeocoderService(places)

    logger.info("Starting gRPC server")

    val grpcServer = NettyServerBuilder
      .forPort(config.grpcPort)
      .addService(ReverseGeocoderGrpcMonix.bindService(reverseGeocoderService, monix.execution.Scheduler.global))
      .build()
      .start()

    sys.ShutdownHookThread {
      grpcServer.shutdown()
      healthttpd.stop()
    }

    healthttpd.indicateReady()
    grpcServer.awaitTermination()
  }
}
