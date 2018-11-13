// Copyright 2018 Vy-Shane Xie

package mu.node.reversegeocoder

import monix.eval.Task
import mu.node.reversegeocoder.ReverseGeocoderGrpcMonix.ReverseGeocoder

class ReverseGeocoderService(reverseGeocodeLocationRpc: ReverseGeocodeLocationRpc) extends ReverseGeocoder {

  override def reverseGeocodeLocation(request: ReverseGeocodeLocationRequest): Task[ReverseGeocodeLocationResponse] = {
    reverseGeocodeLocationRpc.handle(request)
  }
}
