// Copyright 2018 Vy-Shane Xie

package mu.node.reversegeocoder

import java.time.{ZoneId, ZonedDateTime}

import com.google.protobuf.timestamp.Timestamp
import com.thesamet.spatial.KDTreeMap
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import net.time4j.PlainDate
import net.time4j.calendar.astro.{SolarTime, StdSolarCalculator}
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{AsyncWordSpec, Matchers}

class ReverseGeocoderServiceSpec extends AsyncWordSpec with Matchers with AsyncMockFactory {

  "ReverseGeocoderService" when {
    "asked to reverse geocode a location it can't find" should {
      "return an empty response" in {
        val reverseGeocodeService = new ReverseGeocoderService(KDTreeMap(), mock[Clock])
        reverseGeocodeService.reverseGeocodeLocation(ReverseGeocodeLocationRequest(0, 0)).runAsync map { r =>
          r shouldEqual ReverseGeocodeLocationResponse()
        }
      }
    }
    "asked to reverse geocode a location it can find" should {
      "find the nearest place and calculate the sunrise and sunset times at that place" in {
        val perthZoneId = ZoneId.of("Australia/Perth")
        val perthTime = ZonedDateTime.now(perthZoneId)
        val perthLatitude = -31.9505
        val perthLongitude = 115.8605
        val perthElevation = 32

        val clock = mockFunction[ZoneId, Observable[ZonedDateTime]]
        clock.expects(perthZoneId).returns(Observable(perthTime))

        val perthDate = PlainDate.from(perthTime.toLocalDate)
        val solarTime = SolarTime.ofLocation(perthLatitude, perthLongitude, perthElevation, StdSolarCalculator.TIME4J)
        val sunrise = solarTime.sunrise().apply(perthDate).get
        val sunset = solarTime.sunset().apply(perthDate).get

        val perth = Place(
          name = "Perth",
          countryCode = "AU",
          latitude = perthLatitude,
          longitude = perthLongitude,
          elevationMeters = perthElevation,
          timezone = "Australia/Perth",
          population = 1945000,
          sunriseToday = Some(Timestamp(sunrise.getPosixTime, sunrise.getNanosecond)),
          sunsetToday = Some(Timestamp(sunset.getPosixTime, sunset.getNanosecond))
        )

        val reverseGeocodeService = new ReverseGeocoderService(
          KDTreeMap(
            (perthLatitude, perthLongitude) -> perth,
            (-20.26381, 57.4791) -> Place(name = "Quatre Bornes", countryCode = "MU")
          ),
          clock
        )
        reverseGeocodeService
          .reverseGeocodeLocation(ReverseGeocodeLocationRequest(-30, 100))
          .runAsync map { r =>
          r shouldEqual ReverseGeocodeLocationResponse(Option(perth))
        }
      }
    }
  }
}
