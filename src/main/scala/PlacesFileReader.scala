// Copyright 2018 Vy-Shane Xie

package mu.node.reversegeocoder

import com.thesamet.spatial.KDTreeMap
import monix.eval.Task

import scala.util.Try

class PlacesFileReader(loader: LinesFileLoader) {

  def read(filePath: String): Task[KDTreeMap[Location, Place]] = {
    loader(filePath)
      .map(toPlace)
      .map(p => (p.latitude, p.longitude) -> p)
      .toListL
      .map(place => KDTreeMap.fromSeq(place))
  }

  private def toPlace(line: String): Place = {
    val columns = line.split("\t")

    val place = Place(
      name = columns(1),
      countryCode = columns(8),
      latitude = columns(4).toDouble,
      longitude = columns(5).toDouble,
      elevationMeters = Try(columns(15).toInt).getOrElse(0),
      timezone = columns(17),
      population = Try(columns(14).toLong).getOrElse(0)
    )

    if (!columns(3).trim.isEmpty)
      place.copy(alternateNames = columns(3).split(","))
    else
      place
  }
}
