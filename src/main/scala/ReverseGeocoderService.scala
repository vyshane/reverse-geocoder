// Copyright 2018 Vy-Shane Xie

package mu.node.reversegeocoder

import monix.eval.Task
import mu.node.reversegeocoder.ReverseGeocoderGrpcMonix.ReverseGeocoder

class ReverseGeocoderService(config: Config, placesFileReader: PlacesFileReader) extends ReverseGeocoder {

  private implicit val context = monix.execution.Scheduler.Implicits.global
  private val places = placesFileReader.read(config.placesFilePath).memoize  // TODO run immediately and fail on error

  // val belmont = places.findNearest((-31.9450, 115.9270), 1)
  // val nyc = places.findNearest((40.7128, -74.0060), 1)
  // logger.info(belmont.toString)
  // logger.info(nyc.toString)

  override def reverseGeocode(request: ReverseGeocodeRequest): Task[ReverseGeocodeResponse] = {
    for {
      p <- places
      n = p
        .findNearest((request.latitude, request.longitude), 1)
        .headOption
        .map(_._2)
      r = ReverseGeocodeResponse(n)
    } yield (r)
  }
}
