// Copyright 2018 Vy-Shane Xie

package mu.node.reversegeocoder

import io.grpc.netty.NettyServerBuilder
import mu.node.healthttpd.Healthttpd
import wvlet.airframe._

trait Application {
  private val config = bind[Config]
  private val healthttpd = bind[Healthttpd]

  def run: Unit = {
    healthttpd.startAndIndicateNotReady()

    // TODO: Read places file

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
