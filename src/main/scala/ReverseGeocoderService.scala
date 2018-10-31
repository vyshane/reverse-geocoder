// Copyright 2018 Vy-Shane Xie

package mu.node.reversegeocoder

import monix.eval.Task
import mu.node.reversegeocoder.ReverseGeocoderGrpcMonix.ReverseGeocoder
import monix.execution.Scheduler.Implicits.global

import scala.concurrent.Await
import scala.concurrent.duration._

class ReverseGeocoderService(config: Config, placesFileReader: PlacesFileReader) extends ReverseGeocoder {

  // Preload since reading from disk is slow
  private val places = Await.result(placesFileReader.read(config.placesFilePath).runAsync, 1 minute)

  // val belmont = places.findNearest((-31.9450, 115.9270), 1)
  // val nyc = places.findNearest((40.7128, -74.0060), 1)

  override def reverseGeocode(request: ReverseGeocodeRequest): Task[ReverseGeocodeResponse] = {
    val place = places
      .findNearest((request.latitude, request.longitude), 1)
      .headOption
      .map(_._2)
    Task.now(ReverseGeocodeResponse(place))
  }
}
