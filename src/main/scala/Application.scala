// Copyright 2018 Vy-Shane Xie

package mu.node.reversegeocoder

import com.typesafe.scalalogging.LazyLogging
import io.grpc.netty.NettyServerBuilder
import mu.node.healthttpd.Healthttpd
import wvlet.airframe._

trait Application extends LazyLogging {
  private val config = bind[Config]
  private val healthttpd = bind[Healthttpd]
  private val reverseGeocoderService = bind[ReverseGeocoderService]

  def run: Unit = {
    healthttpd.startAndIndicateNotReady()
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
