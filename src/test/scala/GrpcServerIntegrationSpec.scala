// Copyright 2018 Vy-Shane Xie

package mu.node.reversegeocoder

import java.util.UUID

import io.grpc.inprocess.{InProcessChannelBuilder, InProcessServerBuilder}
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import org.scalamock.scalatest.{AsyncMockFactory}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}

// Exercise the gRPC server (in-process)
class GrpcServerIntegrationSpec extends AsyncWordSpec with Matchers with AsyncMockFactory with BeforeAndAfterAll {

  val serverName = s"reverse-geocoder-test-server-${UUID.randomUUID().toString}"
  val reverseGeocoderService = mock[ReverseGeocoderService]

  val server = InProcessServerBuilder
    .forName(serverName)
    .addService(
      ReverseGeocoderGrpcMonix.bindService(reverseGeocoderService, monix.execution.Scheduler.global)
    )
    .directExecutor()
    .build()
    .start()

  val channel = InProcessChannelBuilder
    .forName(serverName)
    .directExecutor()
    .build()

  val reverseGeocoderClient = ReverseGeocoderGrpcMonix.stub(channel)

  override def afterAll(): Unit = {
    channel.shutdownNow()
    server.shutdownNow()
  }

  "The reverse-geocoder gRPC server" when {
    "a request is sent to reverse geocode a location" should {
      "send a response back" in {
        val response = ReverseGeocodeLocationResponse()

        (reverseGeocoderService.reverseGeocodeLocation _)
          .expects(*)
          .returns(Task.now(response))

        reverseGeocoderClient
          .reverseGeocodeLocation(ReverseGeocodeLocationRequest(0, 0))
          .runAsync
          .map { result =>
            result shouldEqual response
          }
      }
    }
  }
}
