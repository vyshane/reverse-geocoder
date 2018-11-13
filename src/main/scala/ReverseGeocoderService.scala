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
    places
      .findNearest((request.latitude, request.longitude), 1)
      .headOption
      .map(_._2)
      .map(place => {
        val now = clock(ZoneId.of(place.timezone)).firstL

        Task.zip2(Task.now(place), now).map {
          case (p, n) =>
            val sun = calculateSun(p.latitude, p.longitude, p.elevationMeters, n)
            val placeWithSun = p.copy(sunriseToday = sun.rise, sunsetToday = sun.set)
            ReverseGeocodeLocationResponse(Some(placeWithSun))
        }
      })
      .getOrElse(Task.now(ReverseGeocodeLocationResponse.defaultInstance))
  }

  private case class Sun(rise: Option[Timestamp], set: Option[Timestamp])

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
}
