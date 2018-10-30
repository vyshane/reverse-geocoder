// Copyright 2018 Vy-Shane Xie

package mu.node.reversegeocoder

import com.typesafe.scalalogging.Logger
import io.grpc.netty.NettyServerBuilder
import mu.node.healthttpd.Healthttpd
import wvlet.airframe._

trait Application {
  private val logger = Logger[this.type]
  private val config = bind[Config]
  private val healthttpd = bind[Healthttpd]
  private val reverseGeocoderService = bind[ReverseGeocoderService]

  def run: Unit = {
    healthttpd.startAndIndicateNotReady()

    val server = NettyServerBuilder
      .forPort(config.grpcPort)
      .addService(ReverseGeocoderGrpcMonix.bindService(reverseGeocoderService, monix.execution.Scheduler.global))
      .build()
      .start()

    sys.ShutdownHookThread {
      server.shutdown()
      healthttpd.stop()
    }

    healthttpd.indicateReady()
    server.awaitTermination()
  }
}
