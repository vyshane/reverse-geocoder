// Copyright 2018 Vy-Shane Xie

package mu.node.reversegeocoder

import java.time.{ZoneId, ZonedDateTime}

import cats.Monad
import cats.implicits._
import com.google.protobuf.timestamp.Timestamp
import com.thesamet.spatial.KDTreeMap
import net.time4j.{Moment, PlainDate}
import net.time4j.calendar.astro.{SolarTime, StdSolarCalculator}

import scala.compat.java8.OptionConverters._

class ReverseGeocodeLocationRpc[F[_]: Monad](places: KDTreeMap[Location, Place], clock: Clock[F], config: Config) {

  def handle(request: ReverseGeocodeLocationRequest): F[ReverseGeocodeLocationResponse] = {
    findNearest(request.latitude, request.longitude)(places)
      .map(p => Monad[F].pure(p))
      .map(addSunTimes(_, clock).map(toResponse))
      .getOrElse(emptyTaskResponse)
  }

  private def findNearest(latitude: Latitude, longitude: Longitude)(places: KDTreeMap[Location, Place]): Option[Place] = {
    places
      .findNearest((latitude, longitude), 1)
      .headOption
      .map(_._2)
  }

  private case class Sun(rise: Option[Timestamp], set: Option[Timestamp])

  private def addSunTimes(place: F[Place], clock: Clock[F]): F[Place] = {
    for {
      p <- place
      c <- clock.now()
      dt = c.atZone(ZoneId.of(p.timezone))
      sun = calculateSun(p.latitude, p.longitude, p.elevationMeters, dt)
      placeWithSun = p.copy(sunriseToday = sun.rise, sunsetToday = sun.set)
    } yield (placeWithSun)
  }

  private def calculateSun(latitude: Latitude,
                           longitude: Longitude,
                           altitudeMeters: Int,
                           zonedDateTime: ZonedDateTime): Sun = {
    val solarTime = SolarTime.ofLocation(latitude, longitude, altitudeMeters, StdSolarCalculator.TIME4J)
    val calendarDate = PlainDate.from(zonedDateTime.toLocalDate)
    def toTimestamp(moment: Moment) = Timestamp(moment.getPosixTime, moment.getNanosecond())
    val rise = solarTime.sunrise().apply(calendarDate).asScala.map(toTimestamp)
    val set = solarTime.sunset().apply(calendarDate).asScala.map(toTimestamp)
    Sun(rise, set)
  }

  private def toResponse(place: Place): ReverseGeocodeLocationResponse = {
    ReverseGeocodeLocationResponse(Some(place))
  }

  private val emptyTaskResponse = Monad[F].pure(ReverseGeocodeLocationResponse.defaultInstance)
}
