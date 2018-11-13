// Copyright 2018 Vy-Shane Xie

package mu.node.reversegeocoder

import java.time.{ZoneId, ZonedDateTime}

import com.google.protobuf.timestamp.Timestamp
import com.thesamet.spatial.KDTreeMap
import monix.eval.Task
import mu.node.reversegeocoder.ReverseGeocoderGrpcMonix.ReverseGeocoder
import net.time4j.{Moment, PlainDate}
import net.time4j.calendar.astro.{SolarTime, StdSolarCalculator}

import scala.compat.java8.OptionConverters._

class ReverseGeocoderService(places: KDTreeMap[Location, Place], clock: Clock) extends ReverseGeocoder {

  override def reverseGeocodeLocation(request: ReverseGeocodeLocationRequest): Task[ReverseGeocodeLocationResponse] = {
    findNearest(request.latitude, request.longitude)(places)
      .map(Task.now(_))
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

  private def addSunTimes(place: Task[Place], clock: Clock): Task[Place] = {
    Task.zip2(place, clock.firstL).map {
      case (p, t) =>
        val zonedDateTime = t.atZone(ZoneId.of(p.timezone))
        val sun = calculateSun(p.latitude, p.longitude, p.elevationMeters, zonedDateTime)
        p.copy(sunriseToday = sun.rise, sunsetToday = sun.set)
    }
  }

  private def calculateSun(latitude: Latitude,
                           longitude: Longitude,
                           altitudeMeters: Int,
                           currentDate: ZonedDateTime): Sun = {
    val solarTime = SolarTime.ofLocation(latitude, longitude, altitudeMeters, StdSolarCalculator.TIME4J)
    val calendarDate = PlainDate.from(currentDate.toLocalDate)
    def toTimestamp(moment: Moment) = Timestamp(moment.getPosixTime, moment.getNanosecond())
    val rise = solarTime.sunrise().apply(calendarDate).asScala.map(toTimestamp)
    val set = solarTime.sunset().apply(calendarDate).asScala.map(toTimestamp)
    Sun(rise, set)
  }

  private def toResponse(place: Place): ReverseGeocodeLocationResponse = {
    ReverseGeocodeLocationResponse(Some(place))
  }

  private val emptyTaskResponse = Task.now(ReverseGeocodeLocationResponse.defaultInstance)
}
