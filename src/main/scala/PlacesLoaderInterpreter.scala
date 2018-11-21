// Copyright 2018 Vy-Shane Xie

package mu.node.reversegeocoder

import cats.Functor
import cats.implicits._
import com.thesamet.spatial.KDTreeMap

import scala.util.Try

// Load places into an in-memory k-d tree for location-based lookups
class PlacesLoaderInterpreter[F[_]: Functor](fileReader: FileReader[F]) extends PlacesLoader[F] {

  override def load(fromPath: String): F[KDTreeMap[Location, Place]] = {
    fileReader
      .readLines(fromPath)
      .map { lines: Seq[String] =>
        lines.map(toPlace)
          .filter(_.isSuccess)
          .map(_.get)
          .map(p => (p.latitude, p.longitude) -> p)
      }
      .map(places => KDTreeMap.fromSeq(places))
  }

  private def toPlace(line: String): Try[Place] = {
    val columns = line.split("\t")
    Try {
      Place(
        name = columns(1),
        countryCode = columns(8),
        latitude = columns(4).toDouble,
        longitude = columns(5).toDouble,
        elevationMeters = Try(columns(15).toInt).getOrElse(0),
        timezone = columns(17),
        population = Try(columns(14).toLong).getOrElse(0)
      )
    }
  }
}
