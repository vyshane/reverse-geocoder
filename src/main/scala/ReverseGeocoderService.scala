// Copyright 2018 Vy-Shane Xie

package mu.node.reversegeocoder

import com.thesamet.spatial.KDTreeMap
import monix.eval.Task
import mu.node.reversegeocoder.ReverseGeocoderGrpcMonix.ReverseGeocoder

class ReverseGeocoderService(places: KDTreeMap[Location, Place]) extends ReverseGeocoder {

  // val belmont = places.findNearest((-31.9450, 115.9270), 1)
  // val nyc = places.findNearest((40.7128, -74.0060), 1)

  override def reverseGeocodeLocation(request: ReverseGeocodeLocationRequest): Task[ReverseGeocodeLocationResponse] = {
    val place = places
      .findNearest((request.latitude, request.longitude), 1)
      .headOption
      .map(_._2)
    Task.now(ReverseGeocodeLocationResponse(place))
  }
}
