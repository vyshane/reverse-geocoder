// Copyright 2018 Vy-Shane Xie

package mu.node.reversegeocoder

import com.thesamet.spatial.KDTreeMap

trait PlacesLoader[F[_]] {

  def load(fromPath: String): F[KDTreeMap[Location, Place]]
}
